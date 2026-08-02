#include "ring_buffer.h"

#include <assert.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

ring_buffer_t* ring_buffer_alloc(size_t capacity) {
    assert(capacity != 0);

    ring_buffer_t* buf = malloc(sizeof(ring_buffer_t));
    if (buf == NULL) {
        return NULL;
    }

    void** queue = calloc(capacity, sizeof(void*));
    if (queue == NULL) {
        free(buf);
        return NULL;
    }

    buf->queue = queue;
    buf->capacity = capacity;
    buf->head = 0;
    buf->tail = 0;
    return buf;
}

void ring_buffer_free(ring_buffer_t* buf) {
    free(buf->queue);
    free(buf);
}

static int ring_buffer_expand(ring_buffer_t* buf, size_t new_capacity) {
    size_t old_capacity = buf->capacity;
    void** new_queue = realloc(buf->queue, new_capacity * sizeof(void*));
    if (!new_queue) {
        return -1;
    }

    buf->queue = new_queue;

    // Move data if the queue loops back
    if (buf->tail < buf->head) {
        size_t head_section_size = old_capacity - buf->head;
        size_t new_head_pos = new_capacity - head_section_size;

        // Move data after head to new queue's end
        memmove(&buf->queue[new_head_pos], &buf->queue[buf->head], head_section_size * sizeof(void*));

        buf->head = new_head_pos;
    }

    buf->capacity = new_capacity;
    return 0;
}

int ring_buffer_enqueue(ring_buffer_t* buf, void* data) {
    size_t used = (buf->tail >= buf->head) ? (buf->tail - buf->head) : (buf->capacity - (buf->head - buf->tail));
    size_t remaining = (buf->capacity - 1) - used;

    if (remaining == 0) {
        // Need to expand
        if (buf->capacity >= SIZE_MAX / 2) {
            return -1;
        }

        int ret = ring_buffer_expand(buf, buf->capacity * 2);
        if (ret) {
            return ret;
        }
    }

    buf->queue[buf->tail++] = data;
    buf->tail = buf->tail % buf->capacity;
    return 0;
}

int ring_buffer_try_enqueue(ring_buffer_t* buf, void* data) {
    size_t used = (buf->tail >= buf->head) ? (buf->tail - buf->head) : (buf->capacity - (buf->head - buf->tail));

    if (used == buf->capacity - 1) {
        return -1;
    }

    buf->queue[buf->tail++] = data;
    buf->tail = buf->tail % buf->capacity;
    return 0;
}

void* ring_buffer_dequeue(ring_buffer_t* buf) {
    // Check whether the queue is empty
    if (buf->head == buf->tail) {
        return NULL;
    }

    void* data = buf->queue[buf->head++];

    // Head reached end of queue
    if (buf->head == buf->capacity) {
        // Reset the head pointer
        buf->head = 0;
    }
    return data;
}
