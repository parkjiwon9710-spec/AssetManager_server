package Market;

public interface QuoteUpdateListener {
    void onQuoteUpdate(String internalSymbol, OvhQuoteParser.Result result);
}