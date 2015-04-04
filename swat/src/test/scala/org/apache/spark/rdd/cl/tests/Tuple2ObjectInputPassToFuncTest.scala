package org.apache.spark.rdd.cl.tests

import java.util.LinkedList
import com.amd.aparapi.internal.writer.BlockWriter.ScalaParameter
import com.amd.aparapi.internal.model.Tuple2ClassModel
import org.apache.spark.rdd.cl.CodeGenTest
import org.apache.spark.rdd.cl.CodeGenUtil
import com.amd.aparapi.internal.model.ClassModel
import com.amd.aparapi.internal.model.HardCodedClassModels

object Tuple2ObjectInputPassToFuncTest extends CodeGenTest[(Int, Point), Float] {
  def getExpectedKernel() : String = {
    "static __global void *alloc(__global void *heap, volatile __global uint *free_index, long heap_size, int nbytes, int *alloc_failed) {\n" +
    "   __global unsigned char *cheap = (__global unsigned char *)heap;\n" +
    "   uint offset = atomic_add(free_index, nbytes);\n" +
    "   if (offset + nbytes > heap_size) { *alloc_failed = 1; return 0x0; }\n" +
    "   else return (__global void *)(cheap + offset);\n" +
    "}\n" +
    "\n" +
    "typedef struct __attribute__ ((packed)) org_apache_spark_rdd_cl_tests_Point_s{\n" +
    "   float  x;\n" +
    "   float  y;\n" +
    "   float  z;\n" +
    "   \n" +
    "} org_apache_spark_rdd_cl_tests_Point;\n" +
    "\n" +
    "typedef struct __attribute__ ((packed)) scala_Tuple2_I_org_apache_spark_rdd_cl_tests_Point_s{\n" +
    "   __global org_apache_spark_rdd_cl_tests_Point  * _2;\n" +
    "   int  _1;\n" +
    "   \n" +
    "} scala_Tuple2_I_org_apache_spark_rdd_cl_tests_Point;\n" +
    "typedef struct This_s{\n" +
    "   } This;\n" +
    "static float org_apache_spark_rdd_cl_tests_Point__z(__global org_apache_spark_rdd_cl_tests_Point *this){\n" +
    "   return this->z;\n" +
    "}\n" +
    "static float org_apache_spark_rdd_cl_tests_Point__y(__global org_apache_spark_rdd_cl_tests_Point *this){\n" +
    "   return this->y;\n" +
    "}\n" +
    "static float org_apache_spark_rdd_cl_tests_Tuple2ObjectInputPassToFuncTest$$anon$1__externalPoint(This *this, __global org_apache_spark_rdd_cl_tests_Point* v){\n" +
    "   return((v->y + v->z));\n" +
    "}\n" +
    "static float org_apache_spark_rdd_cl_tests_Tuple2ObjectInputPassToFuncTest$$anon$1__external(This *this, float v){\n" +
    "   return((v + 3.0f));\n" +
    "}\n" +
    "static float org_apache_spark_rdd_cl_tests_Point__x(__global org_apache_spark_rdd_cl_tests_Point *this){\n" +
    "   return this->x;\n" +
    "}\n" +
    "static float org_apache_spark_rdd_cl_tests_Tuple2ObjectInputPassToFuncTest$$anon$1__apply(This *this, __global scala_Tuple2_I_org_apache_spark_rdd_cl_tests_Point* in){\n" +
    "   return(\n" +
    "   {\n" +
    "       __global org_apache_spark_rdd_cl_tests_Point *p = in->_2;\n" +
    "      (org_apache_spark_rdd_cl_tests_Tuple2ObjectInputPassToFuncTest$$anon$1__external(this, p->x) + org_apache_spark_rdd_cl_tests_Tuple2ObjectInputPassToFuncTest$$anon$1__externalPoint(this, p));\n" +
    "   });\n" +
    "}\n" +
    "__kernel void run(\n" +
    "      __global int * in0_1, __global org_apache_spark_rdd_cl_tests_Point* in0_2, __global scala_Tuple2_I_org_apache_spark_rdd_cl_tests_Point *in0, \n" +
    "      __global float* out, int N) {\n" +
    "   int i = get_global_id(0);\n" +
    "   int nthreads = get_global_size(0);\n" +
    "   This thisStruct;\n" +
    "   This* this=&thisStruct;\n" +
    "   __global scala_Tuple2_I_org_apache_spark_rdd_cl_tests_Point *my_in0 = in0 + get_global_id(0);\n" +
    "   for (; i < N; i += nthreads) {\n" +
    "      my_in0->_1 = in0_1[i];\n" +
    "      my_in0->_2 = in0_2 + i;\n" +
    "      out[i] = org_apache_spark_rdd_cl_tests_Tuple2ObjectInputPassToFuncTest$$anon$1__apply(this, my_in0);\n" +
    "      \n" +
    "   }\n" +
    "}\n"
  }

  def getExpectedNumInputs() : Int = {
    1
  }

  def init() : HardCodedClassModels = {
    val inputClassType1Name = CodeGenUtil.cleanClassName("I")
    val inputClassType2Name = CodeGenUtil.cleanClassName("org.apache.spark.rdd.cl.tests.Point")

    val tuple2ClassModel : Tuple2ClassModel = Tuple2ClassModel.create(
        inputClassType1Name, inputClassType2Name, false)
    val models = new HardCodedClassModels()
    models.addClassModelFor(classOf[Tuple2[_, _]], tuple2ClassModel)
    models
  }

  def complete(params : LinkedList[ScalaParameter]) {
    params.get(0).addTypeParameter("I", false)
    params.get(0).addTypeParameter("Lorg.apache.spark.rdd.cl.tests.Point;", true)
  }

  def getFunction() : Function1[(Int, Point), Float] = {
    new Function[(Int, Point), Float] {
      override def apply(in : (Int, Point)) : Float = {
        val p : Point = in._2
        external(p.x) + externalPoint(p)
      }

      def external(v : Float) : Float = {
        v + 3.0f
      }

      def externalPoint(v : Point) : Float = {
        v.y + v.z
      }
    }
  }
}