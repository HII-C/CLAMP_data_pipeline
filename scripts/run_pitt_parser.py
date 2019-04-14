import threading
import os
import subprocess
import sys

BASE_DIR = "../"
INPUT_DIR = BASE_DIR + "inputdata/completed/"
OUTPUT_DIR = BASE_DIR + "outputdata/"
PARSER_JAR_DIR = BASE_DIR + "PittParser/out/artifacts/CLAMP_data_pipeline_jar/CLAMP_data_pipeline.jar"
LOG_DIR = BASE_DIR + "parselogs/"

# 1st command line argument = username; 2nd commandline argument = db password
if( len(sys.argv) != 3 ):
    sys.exit()

DB_USERNAME = sys.argv[1]
DB_PASS = sys.argv[2]

# Threading vars
MAX_THREAD_COUNT = 6

# danger variables!
thread_file = None
parsing_done = False

def run_parser_on_thread( cv ):
    global thread_file
    global parsing_done

    file_name = None

    with cv:
        while thread_file == None and not parsing_done:
            cv.wait()
        file_name = thread_file
        thread_file = None
        cv.notify()

    if( parsing_done ):
        return

    input_file = INPUT_DIR + file_name
    output_file = OUTPUT_DIR + file_name

    if( not os.path.isdir( input_file ) or not os.path.isdir( output_file ) ):
        print("File does not exist: " + file_name)
        return

    log_file = open( LOG_DIR + "log_" + file_name, "w+" )
    subprocess.call([ "java", "-jar", PARSER_JAR_DIR, DB_USERNAME, DB_PASS, OUTPUT_DIR + file_name, INPUT_DIR + file_name ], stdout=log_file)

    run_parser_on_thread( cv )

def parse():
    global thread_file
    global parsing_done

    active_threads = []

    cv = threading.Condition()
    for _thread_index in range(0, MAX_THREAD_COUNT):
        active_threads.append( threading.Thread( target = run_parser_on_thread, args = ( cv, ) ) )

    for thread in active_threads:
        thread.start()

    output_file_list = os.listdir( OUTPUT_DIR )
    for output_file in output_file_list:
        with cv:
            while( thread_file != None ):
                cv.wait()

            thread_file = output_file
            cv.notify()

    parsing_done = True
    cv.notify_all()

parse()