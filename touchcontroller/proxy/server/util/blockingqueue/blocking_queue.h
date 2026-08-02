#ifndef BLOCKING_QUEUE_H
#define BLOCKING_QUEUE_H

#include <pthread.h>

typedef struct blocking_queue {
    void** buffer;
    int capacity;
    int head;
    int tail;
    int size;

    pthread_mutex_t mutex;
    pthread_cond_t not_full;
    pthread_cond_t not_empty;
} blocking_queue_t;

blocking_queue_t* blocking_queue_create(int capacity);

void blocking_queue_push(blocking_queue_t* queue, void* data);

void* blocking_queue_pop(blocking_queue_t* queue);

int blocking_queue_try_push(blocking_queue_t* queue, void* data);

void* blocking_queue_try_pop(blocking_queue_t* queue);

void blocking_queue_destroy(blocking_queue_t* queue);

#endif
