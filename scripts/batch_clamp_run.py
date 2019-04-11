# After splitting input files into many sub-directories, run the shell script on each of those

import threading
import os
import subprocess
import multiprocessing

BASE_DIR = "../inputdata/"
SUBD_MIN = 0
SUBD_MAX = 4
FILE_NAME_BASE = "batch_"

def run_clamp_on_thread( subdirectory_name ):
    subprocess.call([ "../run_pitt_pipeline.sh", subdirectory_name ]) 

def main():
    threads = []

    # loop through all the files
    for x in range(SUBD_MIN, SUBD_MAX):
        folder = BASE_DIR + FILE_NAME_BASE + str(x)
        threads.append( threading.Thread( target = run_clamp_on_thread, args = (folder,) ) )

    for thread in threads:
        print("starting thread!")
        thread.start()

    for thread in threads:
        thread.join()

    print("Finished CLAMP!!")

main()
