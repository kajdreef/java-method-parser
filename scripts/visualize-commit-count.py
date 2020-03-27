import json
import sys
from pprint import pprint
import matplotlib.pyplot as plt



def histogram(data, title, xaxis, yaxis):
    max_value = max(data)
    n, bins, patches = plt.hist(data, max_value, density=False, facecolor='g', alpha=0.75)
    plt.title(title)
    plt.xlabel(xaxis)
    plt.ylabel(yaxis)
    plt.xlim(0, max_value)
    plt.grid(True)
    plt.show()


if __name__ == "__main__":
    file_name = sys.argv[1]

    print(file_name)
    with open(file_name) as json_file:
        content = json.load(json_file)
        
        count = []

        print(content["sut"])
        for m in content["methods"]:
            count.append(m["commits-count"])

        histogram(count, "Historgram of number of changes to methods", "number of changes to a single method", "Frequency of x changes per method")