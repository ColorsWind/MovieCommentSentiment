import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
# os.environ["CUDA_DEVICE_ORDER"] = "PCI_BUS_ID"  
# os.environ["CUDA_VISIBLE_DEVICES"] = "-1"

from gensim.models import word2vec
from tensorflow import keras
from keras import layers
from time import time
import numpy as np
import pandas as pd
import jieba

import bz2
import re
import sys

import logging
jieba.setLogLevel(logging.INFO)

class TransformerBlock(layers.Layer):
    def __init__(self, att=None, ffn=None, layernorm1=None, layernorm2=None, dropout1=None, dropout2=None,
                 embed_dim=None, num_heads=None, ff_dim=None, rate=0.1):
        super(TransformerBlock, self).__init__()
        if att is not None:
            self.att = att
            self.ffn = ffn
            self.layernorm1 = layernorm1
            self.layernorm2 = layernorm2
            self.dropout1 = dropout1
            self.dropout2 = dropout2
        else:
            self.att = layers.MultiHeadAttention(num_heads=num_heads, key_dim=embed_dim)
            self.ffn = keras.Sequential(
                [layers.Dense(ff_dim, activation="relu"), layers.Dense(embed_dim), ]
            )
            self.layernorm1 = layers.LayerNormalization(epsilon=1e-6)
            self.layernorm2 = layers.LayerNormalization(epsilon=1e-6)
            self.dropout1 = layers.Dropout(rate)
            self.dropout2 = layers.Dropout(rate)

    def call(self, inputs, training):
        attn_output = self.att(inputs, inputs)
        attn_output = self.dropout1(attn_output, training=training)
        out1 = self.layernorm1(inputs + attn_output)
        ffn_output = self.ffn(out1)
        ffn_output = self.dropout2(ffn_output, training=training)
        return self.layernorm2(out1 + ffn_output)

    def get_config(self):
        return {
            "att": self.att,
            "ffn": self.ffn,
            "layernorm1": self.layernorm1,
            "layernorm2": self.layernorm2,
            "dropout1": self.dropout1,
            "dropout2": self.dropout2
        }

    @classmethod
    def from_config(cls, config):
        return cls(**config)

class SpiltIter(object):
    match = re.compile('[\w]+')

    def __init__(self, text):
        self.iter = jieba.cut(text, cut_all=False)

    def __iter__(self):
        return self

    def __next__(self):
        while True:
            token = next(self.iter)
            if self.match.fullmatch(token) and token not in stop_words:
                return token

def predict(text):
    cut_list = list(SpiltIter(text))
    vec = np.zeros([len(cut_list)], dtype='int')
    for i, word in enumerate(cut_list):
        try:
            vec[i] = key_to_index[word]
        except KeyError:
            vec[i] = 0
    text_vec = keras.preprocessing.sequence.pad_sequences([vec], maxlen=max_tokens,
                                                          padding='pre', truncating='pre')
    text_vec[text_vec >= num_words] = 0
    return model.predict(x=text_vec)[0][1]

max_tokens = 100
num_words = 50000
stop_df = pd.read_csv('cn_stopwords.txt')
stop_words = set(stop_df.values[:,0])

if __name__ == "__main__":
    begin_time = time()
    model = keras.models.load_model('transformer.h5', custom_objects={'TransformerBlock': TransformerBlock})
    key_to_index = pd.read_csv('key_to_index.csv', index_col=0).to_dict()['index']
    end_time = time()
    if len(sys.argv) > 1 and sys.argv[1] == 'unicode':
        while True:
            ch = input()
            text = ch
            text = ch.encode('utf-8').decode('unicode_escape')
            print(predict(text))
    else:
        print('Loading model completed: ', (end_time - begin_time) * 1000, ' ms')
        while True:
            ch = input()
            text = ch
            print(predict(ch))