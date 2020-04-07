import json
import sys
from pprint import pprint
import matplotlib.pyplot as plt
from datetime import datetime


def histogram(data, title, xaxis, yaxis):
    max_value = max(data)
    n, bins, patches = plt.hist(data, round(max_value/250), density=False, facecolor='g', alpha=0.75)
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
        relative_date = []
        for change in content["methods"]:
        
            if (len(change["commits"]) == 0):
                continue
            
            print(change["commits"])
        
            dates = list(map(lambda date: datetime.strptime(date, '%Y-%m-%d'), map(lambda element : element['date'], change["commits"])))
            dates.sort()
            introduction = dates[0]

            for date in dates:
                delta = date - introduction
                relative_date.append(delta.days)

            
        histogram(relative_date, "Distribution of commits after method was introduced", "Days since introduction", "Number of commits made")