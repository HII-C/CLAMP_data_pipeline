import glob
import os

PARSE_DIR = "../parselogs/"
CONDENSED_FILE = PARSE_DIR + "condensed.txt"

file_names = os.listdir(PARSE_DIR)
file_names.sort()

with open( CONDENSED_FILE, "wb" ) as outfile:
    for file in file_names:
        with open( file, "rb" ) as infile:
            outfile.write( infile.read() )