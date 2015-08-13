#!/bin/bash

if [[ $# != 2 ]]; then
    echo usage: run.sh niters use-swat?
    exit 1
fi

# --conf "spark.executor.extraJavaOptions=-Xloggc:/tmp/SWAT.log -XX:-PrintGC -XX:-PrintGCDetails -XX:-PrintGCTimeStamps -XX:-PrintCompilation -XX:-CITime -verbose:gc -verbose:jni" \

spark-submit --class SparkFuzzyCMeans \
        --jars ${SWAT_HOME}/swat/target/swat-1.0-SNAPSHOT.jar,${APARAPI_HOME}/com.amd.aparapi/dist/aparapi.jar,${ASM_HOME}/lib/asm-5.0.3.jar,${ASM_HOME}/lib/asm-util-5.0.3.jar \
        --conf "spark.executor.extraJavaOptions=-XX:+UseG1GC -XX:-ResizePLAB" \
        --conf "spark.executor.memory=40g" \
        --master spark://localhost:7077 \
        ${SWAT_HOME}/functional-tests/fuzzy-cmeans/target/sparkfuzzycmeans-0.0.0.jar \
        run 40 $1 hdfs://$(hostname):54310/converted $2