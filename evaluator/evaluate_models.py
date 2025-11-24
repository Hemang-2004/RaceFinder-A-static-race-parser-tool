import json
import torch
import numpy as np
from pathlib import Path
from typing import Dict, List
from sklearn.metrics import accuracy_score, precision_recall_fscore_support
import matplotlib.pyplot as plt
from transformers import AutoTokenizer, AutoModelForSequenceClassification, AutoModelForCausalLM


# ============================================================
# ⚙️ Configuration
# ============================================================

REPORT_PATH = "/Users/knight_striker/Desktop/RaceFinder-A-static-race-parser-tool/parser/race_report.json"
LABELS_PATH = "labels.json"

OUTPUT_JSON = "llm_scores.json"
OUTPUT_PLOT = "llm_comparison.png"

LLM_MODELS = [
    "bert-base-uncased",
    "bert-large-uncased",
    "distilbert-base-uncased",
    "roberta-base",
    "roberta-large",
    "albert-base-v2",
    "google/electra-base-discriminator",
    "microsoft/MiniLM-L12-H384-uncased",
    "xlnet-base-cased",
    "gpt2",
    "gpt2-medium",
    "gpt2-large",
]


DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")


# ============================================================
# 📌 Load dataset
# ============================================================

def load_dataset(report_path: str, labels_path: str):
    report = json.loads(Path(report_path).read_text())
    labels = json.loads(Path(labels_path).read_text())

    texts = []
    y = []

    for entry in report:
        key = f"{entry['location']}::{entry['type']}"
        if key not in labels:
            continue

        text = f"{entry['threat']} | {entry['type']} | {entry['description']}"
        texts.append(text)
        y.append(labels[key])

    y = np.array(y, dtype=int)

    return texts, y


# ============================================================
# 🧠 LLM Zero-Shot Classification
# ============================================================

def classify_with_encoder_model(model_name: str, text: str) -> int:
    """
    For encoder models (BERT, RoBERTa, Electra, Albert, XLNet).
    We convert this into a binary classification.
    """

    tokenizer = AutoTokenizer.from_pretrained(model_name)
    model = AutoModelForSequenceClassification.from_pretrained(
        model_name, num_labels=2
    ).to(DEVICE)

    encoded = tokenizer(
        text,
        truncation=True,
        padding=True,
        return_tensors="pt"
    ).to(DEVICE)

    with torch.no_grad():
        logits = model(**encoded).logits
        pred = torch.argmax(logits, dim=1).item()

    return pred  # 0 or 1


def classify_with_gpt_model(model_name: str, text: str) -> int:
    """
    For causal models (GPT-2 family).
    We use a logit-comparison trick:
    - Score the tokens for " yes" vs " no"
    """

    tokenizer = AutoTokenizer.from_pretrained(model_name)
    model = AutoModelForCausalLM.from_pretrained(model_name).to(DEVICE)

    prompt = (
        text
        + "\n\nQuestion: Is this a real race-condition risk? Answer yes or no.\nAnswer:"
    )

    inputs = tokenizer(prompt, return_tensors="pt").to(DEVICE)

    with torch.no_grad():
        logits = model(**inputs).logits[0, -1]

    yes_id = tokenizer.encode(" yes", add_special_tokens=False)[0]
    no_id = tokenizer.encode(" no", add_special_tokens=False)[0]

    yes_score = logits[yes_id].item()
    no_score = logits[no_id].item()

    return 1 if yes_score > no_score else 0


# ============================================================
# 📝 Evaluate all LLMs
# ============================================================

def evaluate_model(model_name: str, texts: List[str], true_labels: np.ndarray) -> Dict[str, float]:
    preds = []

    print(f"\n🔥 Running model: {model_name}")

    is_gpt = model_name.startswith("gpt2")

    for text in texts:
        if is_gpt:
            pred = classify_with_gpt_model(model_name, text)
        else:
            pred = classify_with_encoder_model(model_name, text)
        preds.append(pred)

    preds = np.array(preds)

    acc = accuracy_score(true_labels, preds)
    precision, recall, f1, _ = precision_recall_fscore_support(
        true_labels, preds, average="binary", zero_division=0
    )

    return {
        "accuracy": float(acc),
        "precision": float(precision),
        "recall": float(recall),
        "f1": float(f1),
    }


# ============================================================
# 📊 Plot Results
# ============================================================

def plot_scores(scores: Dict[str, Dict[str, float]]):
    models = list(scores.keys())
    acc = [scores[m]["accuracy"] for m in models]
    prec = [scores[m]["precision"] for m in models]
    rec = [scores[m]["recall"] for m in models]
    f1 = [scores[m]["f1"] for m in models]

    fig, axs = plt.subplots(2, 2, figsize=(16, 10))
    fig.suptitle("LLM Model Comparison")

    def bar(ax, vals, title):
        ax.bar(models, vals)
        ax.set_title(title)
        ax.set_ylim(0, 1)
        ax.tick_params(axis='x', rotation=45)
        for i, v in enumerate(vals):
            ax.text(i, v + 0.01, f"{v:.2f}", ha="center")

    bar(axs[0, 0], acc, "Accuracy")
    bar(axs[0, 1], prec, "Precision")
    bar(axs[1, 0], rec, "Recall")
    bar(axs[1, 1], f1, "F1 Score")

    plt.tight_layout()
    plt.savefig(OUTPUT_PLOT)
    plt.close()


# ============================================================
# 🚀 Main
# ============================================================

def main():
    print("📥 Loading dataset...")
    texts, y = load_dataset(REPORT_PATH, LABELS_PATH)

    print(f"Loaded {len(texts)} labeled samples")

    all_scores = {}

    for model_name in LLM_MODELS:
        try:
            scores = evaluate_model(model_name, texts, y)
            all_scores[model_name] = scores
            print(f"✔ Completed {model_name}: {scores}")
        except Exception as e:
            print(f"❌ FAILED {model_name}: {e}")

    Path(OUTPUT_JSON).write_text(json.dumps(all_scores, indent=4))
    print(f"\n📄 Saved JSON: {OUTPUT_JSON}")

    plot_scores(all_scores)
    print(f"📊 Saved chart: {OUTPUT_PLOT}")


if __name__ == "__main__":
    main()
