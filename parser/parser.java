package parser;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.json.*;

import java.io.*;
import java.util.*;


public class parser {

    public enum ThreatLevel { HIGH, MEDIUM, LOW }

    public static void main(String[] args) throws Exception {

        // Parser configuration (NON-DEPRECATED)
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        StaticJavaParser.setConfiguration(config);

        Map<String, JSONObject> collector = new LinkedHashMap<>();

        // ROOT PATH FOR YOUR BACKEND
        File root = new File("../bank");

        scan(root, collector);

        List<JSONObject> finalList = new ArrayList<>(collector.values());
        writeJsonReport(finalList);

        System.out.println("✔ race_report.json generated into /parser folder");
    }


    // =====================================================================
    // SCAN BACKEND FOLDERS
    // =====================================================================
    static void scan(File dir, Map<String, JSONObject> collector) {

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {

            if (f.isDirectory()) {
                scan(f, collector);
                continue;
            }

            if (!f.getName().endsWith(".java")) continue;

            try {
                CompilationUnit cu = StaticJavaParser.parse(f);
                cu.accept(new RaceVisitor(f.getPath(), collector), null);
            } catch (Exception ignored) {}
        }
    }


    // =====================================================================
    // WRITE JSON OUTPUT
    // =====================================================================
    private static void writeJsonReport(List<JSONObject> list) {

        String output = "../parser/race_report.json";

        try (FileWriter fw = new FileWriter(output)) {
            fw.write(new JSONArray(list).toString(4));
        } catch (Exception e) {
            System.err.println("ERROR writing race report: " + e.getMessage());
        }
    }


    // =====================================================================
    // VISITOR — DETECT RACES
    // =====================================================================
    static class RaceVisitor extends VoidVisitorAdapter<Void> {

        String filePath;
        Map<String, JSONObject> out;

        RaceVisitor(String file, Map<String, JSONObject> out) {
            this.filePath = file;
            this.out = out;
        }


        void addRace(String variable, String type, ThreatLevel level, String method) {

            JSONObject obj = new JSONObject();
            obj.put("file", filePath);
            obj.put("variable", variable);
            obj.put("type", type);
            obj.put("threat", level.toString());
            obj.put("method", method);
            obj.put("description", "Method '" + method + "' flagged: " + type);

            String key = filePath + "|" + method + "|" + type;
            out.put(key, obj);
        }


        @Override
        public void visit(MethodDeclaration m, Void arg) {
            super.visit(m, arg);

            detectLostUpdate(m);
            detectCheckThenWrite(m);
            detectCheckThenCall(m);
            detectWriteThenRead(m);
        }


        // =====================================================================
        // PATTERN 1 — LOST UPDATE: find* -> modify -> save
        // =====================================================================
        void detectLostUpdate(MethodDeclaration m) {

            List<MethodCallExpr> reads = m.findAll(
                    MethodCallExpr.class,
                    c -> c.getNameAsString().toLowerCase().contains("find")
            );

            for (MethodCallExpr readCall : reads) {

                Optional<String> var = getAssignedVar(readCall);
                if (var.isEmpty()) continue;

                String v = var.get();

                Optional<MethodCallExpr> write = m.findAll(
                        MethodCallExpr.class,
                        c -> c.getNameAsString().toLowerCase().contains("save")
                                && c.toString().contains(v)
                ).stream().findFirst();

                if (write.isPresent()) {
                    addRace(v, "Lost Update (Read-Modify-Write)", ThreatLevel.HIGH, m.getNameAsString());
                }
            }
        }


        // =====================================================================
        // PATTERN 2 — CHECK-THEN-WRITE
        // =====================================================================
        void detectCheckThenWrite(MethodDeclaration m) {

            for (IfStmt ifs : m.findAll(IfStmt.class)) {

                boolean writesToRepo = !ifs.getThenStmt()
                        .findAll(MethodCallExpr.class,
                                c -> c.getNameAsString().toLowerCase().contains("save"))
                        .isEmpty();

                if (writesToRepo) {
                    addRace("cond", "Inconsistent Read (Check-Then-Write)", ThreatLevel.HIGH, m.getNameAsString());
                }
            }
        }


        // =====================================================================
        // PATTERN 3 — CHECK-THEN-CALL (service calls)
        // =====================================================================
        void detectCheckThenCall(MethodDeclaration m) {

            for (IfStmt ifs : m.findAll(IfStmt.class)) {

                boolean callsService =
                        !ifs.getThenStmt().findAll(MethodCallExpr.class,
                                c -> c.getNameAsString().toLowerCase().contains("service"))
                                .isEmpty();

                if (callsService) {
                    addRace("cond", "Inconsistent Read (Check-Then-Call)", ThreatLevel.MEDIUM, m.getNameAsString());
                }
            }
        }


        // =====================================================================
        // PATTERN 4 — WRITE-THEN-READ (SAVE then FIND later)
        // =====================================================================
        void detectWriteThenRead(MethodDeclaration m) {

            List<MethodCallExpr> writes = m.findAll(
                    MethodCallExpr.class,
                    c -> c.getNameAsString().toLowerCase().contains("save")
            );

            if (writes.isEmpty()) return;

            MethodCallExpr firstWrite = writes.get(0);

            boolean readsAfter = !m.findAll(
                    MethodCallExpr.class,
                    c ->
                            c.getNameAsString().toLowerCase().contains("find") &&
                            c.getBegin().isPresent() &&
                            firstWrite.getBegin().isPresent() &&
                            c.getBegin().get().isAfter(firstWrite.getBegin().get())
            ).isEmpty();

            if (readsAfter) {
                addRace("repo", "Write-Then-Read", ThreatLevel.LOW, m.getNameAsString());
            }
        }


        // =====================================================================
        // UTILITY: extract variable assigned from a method call
        // =====================================================================
        Optional<String> getAssignedVar(MethodCallExpr m) {

            Node p = m.getParentNode().orElse(null);

            while (p != null) {

                if (p instanceof AssignExpr) {
                    return Optional.of(((AssignExpr) p).getTarget().toString());
                }

                if (p instanceof VariableDeclarator) {
                    return Optional.of(((VariableDeclarator) p).getNameAsString());
                }

                p = p.getParentNode().orElse(null);
            }

            return Optional.empty();
        }
    }
}
