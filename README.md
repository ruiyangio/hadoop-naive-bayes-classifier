# hadoop-naive-bayes-classifier
Naive Bayes classifier for sentiment analysis

# Multinomial Naive Bayes
The hadoop job implements Multinomial Naive Bayes with uni-gram.

Laplace smoothing is used.

# Model format
The job generates a vocabulary with each word's log likeihood for further use.

Example:
```javascript
{
    "token":"adept",
    "positive":"-11.910391",
    "negative":"-12.852409",
    "positivePrior":"-0.693147",
    "negativePrior":"-0.693147"
}
```

# Data set and accuracy
Imdb review data set is used: [Large Movie Review Dataset](http://ai.stanford.edu/~amaas/data/sentiment/)

The dataset is transformed to a format of \<review>@@@@\<POS|NEG> for hadoop job processing.

Accuracy: 81%
