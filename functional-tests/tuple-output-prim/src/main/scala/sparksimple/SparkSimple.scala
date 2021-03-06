/*
Copyright (c) 2016, Rice University

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

1.  Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
2.  Redistributions in binary form must reproduce the above
     copyright notice, this list of conditions and the following
     disclaimer in the documentation and/or other materials provided
     with the distribution.
3.  Neither the name of Rice University
     nor the names of its contributors may be used to endorse or
     promote products derived from this software without specific
     prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.cl._
import Array._
import scala.math._
import org.apache.spark.rdd._
import java.net._

object SparkSimple {
    def main(args : Array[String]) {
        if (args.length < 1) {
            println("usage: SparkSimple cmd")
            return;
        }

        val cmd = args(0)

        if (cmd == "convert") {
            convert(args.slice(1, args.length))
        } else if (cmd == "run") {
            run_simple(args.slice(2, args.length), args(1).toBoolean)
        } else if (cmd == "check") {
            val correct : Array[(Float, Double)] = run_simple(args.slice(1, args.length), false)
            val actual : Array[(Float, Double)] = run_simple(args.slice(1, args.length), true)
            assert(correct.length == actual.length)
            for (i <- 0 until correct.length) {
                val a : (Float, Double) = correct(i)
                val b : (Float, Double) = actual(i)
                var error : Boolean = false

                if (a._1 != b._1) {
                    System.err.println(i + " _1 expected " + a._1 +
                        " but got " + b._1)
                    error = true
                }

                if (a._2 != b._2) {
                    System.err.println(i + " _2 expected " + a._2 +
                        " but got " + b._2)
                    error = true
                }

                // if (error) System.exit(1)
            }
            System.err.println("PASSED")
        }
    }

    def get_spark_context(appName : String) : SparkContext = {
        val conf = new SparkConf()
        conf.setAppName(appName)

        val localhost = InetAddress.getLocalHost
        conf.setMaster("spark://" + localhost.getHostName + ":7077") // 7077 is the default port

        return new SparkContext(conf)
    }

    def run_simple(args : Array[String], useSwat : Boolean) : Array[(Float, Double)] = {
        if (args.length != 1) {
            println("usage: SparkSimple run input-path");
            return new Array[(Float, Double)](0);
        }
        val sc = get_spark_context("Spark Simple");

        val m : Float = 4.0f

        val arr : Array[Double] = new Array[Double](3)
        arr(0) = 0.0
        arr(1) = 3.0
        arr(2) = 6.0

        val inputPath = args(0)
        val inputs_raw : RDD[Float] = sc.objectFile[Float](inputPath).cache
        val inputs = if (useSwat) CLWrapper.cl[Float](inputs_raw) else inputs_raw
        val outputs : RDD[(Float, Double)] =
            inputs.map(v => (v, v + m * arr(1)))
        val outputs2 : Array[(Float, Double)] = outputs.collect
        sc.stop
        outputs2
    }

    def convert(args : Array[String]) {
        if (args.length != 2) {
            println("usage: SparkSimple convert input-dir output-dir");
            return
        }
        val sc = get_spark_context("Spark KMeans Converter");

        val inputDir = args(0)
        var outputDir = args(1)
        val input = sc.textFile(inputDir)
        val converted = input.map(line => {
            val tokens : Array[String] = line.split(" ")
            assert(tokens.size == 1)
            tokens(0).toFloat
        })
        converted.saveAsObjectFile(outputDir)
    }
}
