package rui.classifier.bayes;

public enum BayesCounter {
    PositiveCounter,
    NegativeCounter,
    PositiveDocument,
    NegativeDocument,
    UniqueTokenCounter;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "." + name();
    }
}
