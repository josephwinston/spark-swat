import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.cl._
import Array._
import scala.math._
import org.apache.spark.rdd._
import java.net._

class Point(val x: Float, val y: Float, val z: Float)
    extends java.io.Serializable {
  def this() {
    this(0.0f, 0.0f, 0.0f)
  }
}

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
            run_simple(args.slice(1, args.length))
        } else if (cmd == "run-cl") {
            run_simple_cl(args.slice(1, args.length))
        } else if (cmd == "check") {
            val correct : Array[Float] = run_simple(args.slice(1, args.length))
            val actual : Array[Float] = run_simple_cl(args.slice(1, args.length))
            assert(correct.length == actual.length)
            for (i <- 0 until correct.length) {
                val a : Float = correct(i)
                val b : Float = actual(i)
                var error : Boolean = false

                if (a != b) {
                    System.err.println(i + " expected " + a + " but got " + b)
                    error = true
                }

                if (error) System.exit(1)
            }
        }
    }

    def get_spark_context(appName : String) : SparkContext = {
        val conf = new SparkConf()
        conf.setAppName(appName)

        val localhost = InetAddress.getLocalHost
        conf.setMaster("spark://" + localhost.getHostName + ":7077") // 7077 is the default port

        return new SparkContext(conf)
    }

    def run_simple(args : Array[String]) : Array[Float] = {
        if (args.length != 1) {
            println("usage: SparkSimple run input-path");
            return new Array[Float](0);
        }
        val sc = get_spark_context("Spark Simple");

        val m : Float = 4.0f

        val arr : Array[Point] = new Array[Point](3)
        arr(0) = new Point(0, 1, 2)
        arr(1) = new Point(3, 4, 5)
        arr(2) = new Point(6, 7, 8)

        val inputPath = args(0)
        val inputs : RDD[(Int, Point)] = sc.objectFile[(Int, Point)](inputPath).cache
        val outputs : RDD[Float] = inputs.map(v => v._1 + v._2.x + v._2.y + v._2.z + m + arr(1).x)
        val outputs2 : Array[Float] = outputs.collect
        sc.stop
        outputs2
    }

    def run_simple_cl(args : Array[String]) : Array[Float] = {
        if (args.length != 1) {
            println("usage: SparkSimple run-cl input-path");
            return new Array[Float](0)
        }
        val sc = get_spark_context("Spark Simple");

        val m : Float = 4.0f

        val arr : Array[Point] = new Array[Point](3)
        arr(0) = new Point(0, 1, 2)
        arr(1) = new Point(3, 4, 5)
        arr(2) = new Point(6, 7, 8)

        val inputPath = args(0)
        val inputs : RDD[(Int, Point)] = sc.objectFile[(Int, Point)](inputPath).cache
        val inputs_cl : CLWrapperRDD[(Int, Point)] = CLWrapper.cl[(Int, Point)](inputs)
        val outputs : RDD[Float] = inputs_cl.map(v => v._1 + v._2.x + v._2.y + v._2.z + m + arr(1).x)
        val outputs2 : Array[Float] = outputs.collect
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
            assert(tokens.size == 4)
            ( tokens(0).toInt, new Point(tokens(0).toFloat, tokens(1).toFloat,
                    tokens(2).toFloat) ) })
        converted.saveAsObjectFile(outputDir)
    }
}