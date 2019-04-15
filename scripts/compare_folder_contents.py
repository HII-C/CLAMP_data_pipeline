import os

OUTPUT_PATH = "../outputdata/"
INPUT_PATH = "../inputdata/completed/"

file_dict = {}

for filename in os.listdir(INPUT_PATH):
  if filename.endswith(".txt"):
    file_dict[filename] = True

for filename in os.listdir(OUTPUT_PATH):
  if filename.endswith(".txt") and not file_dict.get(filename):
    print("Following file is not in completed folder: " + filename)