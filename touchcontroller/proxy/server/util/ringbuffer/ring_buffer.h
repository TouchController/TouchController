#ifndef RING_BUFFER_H
#define RING_BUFFER_H

#include <stddef.h>

typedef struct ring_buffer {
    void** queue;
    size_t capacity;
    size_t head;
    size_t tail;
} ring_buffer_t;

ring_buffer_t* ring_buffer_alloc(size_t capacity);
void ring_buffer_free(ring_buffer_t* buf);
int ring_buffer_enqueue(ring_buffer_t* buf, void* data);
int ring_buffer_try_enqueue(ring_buffer_t* buf, void* data);
void* ring_buffer_dequeue(ring_buffer_t* buf);

#endif
