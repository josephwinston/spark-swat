#ifndef BRIDGE_H
#define BRIDGE_H

#include <assert.h>
#include <string.h>
#include <stdio.h>
#include <map>
#include <set>
#include <string>
#include <pthread.h>

#include "common.h"
#include "kernel_arg.h"
#include "allocator.h"

using namespace std;

#ifdef __APPLE__
#include <OpenCL/opencl.h>
#else
#include <CL/cl.h>
#endif

#define JNI_JAVA(type, className, methodName) JNIEXPORT type JNICALL Java_org_apache_spark_rdd_cl_##className##_##methodName

class mem_and_size {
    public:
        mem_and_size(cl_region *set_mem, size_t set_size) : mem(set_mem),
            size(set_size), valid(true) { }
        mem_and_size() : valid(false) { }

        cl_region *get_mem() { assert(valid); return mem; }
        size_t get_size() { assert(valid); return size; }
        bool is_valid() { return valid; }
    private:
        cl_region *mem;
        size_t size;
        bool valid;
};

class rdd_partition_offset {
    public:
        rdd_partition_offset(int set_rdd, int set_index, int set_offset, int set_component) :
            rdd(set_rdd), index(set_index), offset(set_offset), component(set_component) { }

        bool operator<(const rdd_partition_offset& other) const {
            if (rdd < other.rdd) {
                return true;
            } else if (rdd > other.rdd) {
                return false;
            }

            if (index < other.index) {
                return true;
            } else if (index > other.index) {
                return false;
            }

            if (offset < other.offset) {
                return true;
            } else if (offset > other.offset) {
                return false;
            }

            return component < other.component;
        }
    private:
        // The RDD this buffer is a member of
        int rdd;
        // The partition in rdd
        int index;
        // The offset in elements inside the partition
        int offset;
        /*
         * The component of this buffer we are storing (e.g. multiple buffers
         * are necessary to represent Tuple2 RDDs
         */
        int component;
};

typedef struct _device_context {
    cl_platform_id platform;
    cl_device_id dev;
    cl_context ctx;
    cl_command_queue cmd;

    pthread_mutex_t lock;

    cl_allocator *allocator;

    map<string, cl_program> *program_cache;
    map<jlong, pair<cl_region *, size_t> > *broadcast_cache;
    map<rdd_partition_offset, pair<cl_region *, size_t> > *rdd_cache;
    // map<rdd_partition_offset, mem_and_size> *rdd_cache;
} device_context;

typedef struct _swat_context {
    cl_kernel kernel;
    int host_thread_index;

    map<int, pair<cl_region *, bool> > *arguments;
#ifdef BRIDGE_DEBUG
    map<int, kernel_arg *> *debug_arguments;
    char *kernel_src;
    size_t kernel_src_len;
#endif

} swat_context;

#endif
