#include "blocking_queue.h"

#include <assert.h>
#include <stdlib.h>
#include <unistd.h>

blocking_queue_t* blocking_queue_create(int capacity) {
    assert(capacity > 0);

    blocking_queue_t* queue = (blocking_queue_t*)malloc(sizeof(blocking_queue_t));
    if (!queue) {
        return NULL;
    }

    queue->buffer = (void**)malloc(sizeof(void*) * capacity);
    if (queue->buffer == NULL) {
        free(queue);
        return NULL;
    }

    queue->capacity = capacity;
    queue->head = 0;
    queue->tail = 0;
    queue->size = 0;

    pthread_mutex_init(&queue->mutex, NULL);
    pthread_cond_init(&queue->not_full, NULL);
    pthread_cond_init(&queue->not_empty, NULL);

    return queue;
}

void blocking_queue_push(blocking_queue_t* queue, void* data) {
    assert(data != NULL);
    pthread_mutex_lock(&queue->mutex);

    while (queue->size == queue->capacity) {
        pthread_cond_wait(&queue->not_full, &queue->mutex);
    }

    queue->buffer[queue->tail] = data;
    queue->tail = (queue->tail + 1) % queue->capacity;
    queue->size++;

    pthread_cond_signal(&queue->not_empty);

    pthread_mutex_unlock(&queue->mutex);
}

void* blocking_queue_pop(blocking_queue_t* queue) {
    pthread_mutex_lock(&queue->mutex);

    while (queue->size == 0) {
        pthread_cond_wait(&queue->not_empty, &queue->mutex);
    }

    void* data = queue->buffer[queue->head];
    queue->head = (queue->head + 1) % queue->capacity;
    queue->size--;

    pthread_cond_signal(&queue->not_full);

    pthread_mutex_unlock(&queue->mutex);
    return data;
}

int blocking_queue_try_push(blocking_queue_t* queue, void* data) {
    assert(data != NULL);
    pthread_mutex_lock(&queue->mutex);

    if (queue->size == queue->capacity) {
        pthread_mutex_unlock(&queue->mutex);
        return 1;
    }

    queue->buffer[queue->tail] = data;
    queue->tail = (queue->tail + 1) % queue->capacity;
    queue->size++;

    pthread_cond_signal(&queue->not_empty);

    pthread_mutex_unlock(&queue->mutex);
    return 0;
}

void* blocking_queue_try_pop(blocking_queue_t* queue) {
    pthread_mutex_lock(&queue->mutex);

    if (queue->size == 0) {
        pthread_mutex_unlock(&queue->mutex);
        return NULL;
    }

    void* data_ptr = queue->buffer[queue->head];
    queue->head = (queue->head + 1) % queue->capacity;
    queue->size--;

    pthread_cond_signal(&queue->not_full);

    pthread_mutex_unlock(&queue->mutex);
    return data_ptr;
}

void blocking_queue_destroy(blocking_queue_t* queue) {
    if (!queue) return;

    pthread_cond_signal(&queue->not_full);
    pthread_cond_signal(&queue->not_empty);

    pthread_mutex_destroy(&queue->mutex);
    pthread_cond_destroy(&queue->not_full);
    pthread_cond_destroy(&queue->not_empty);

    free(queue->buffer);
    free(queue);
}
