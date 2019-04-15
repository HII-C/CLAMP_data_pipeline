import os

OUTPUT_PATH = "../outputdata/"
INPUT_PATH = "../inputdata/completed/"

file_dict = {}

input_count = 0
output_count = 0
for filename in os.listdir(INPUT_PATH):
  if filename.endswith(".txt"):
    file_dict[filename] = True
  input_count += 1

for filename in os.listdir(OUTPUT_PATH):
  if filename.endswith(".txt") and not file_dict.get(filename):
    print("Following file is not in completed folder: " + filename)
  output_count += 1

print("Input count: " + str(input_count))
print("Output count: " + str(output_count))