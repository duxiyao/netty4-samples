package com;

import java.util.Comparator;
import java.util.PriorityQueue;

//计算最大最小值，平均值，中位数
public class StreamNum {

    PriorityQueue<Long> minHeap = new PriorityQueue<>(1); //小顶堆，默认容量为11
    PriorityQueue<Long> maxHeap = new PriorityQueue<>(1, new Comparator<Long>() {
        @Override
        public int compare(Long o1, Long o2) {
            try {
                return (int) (o2 - o1);//降序排列
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        } //大顶堆，容量11

    });
    private double sum;
    private Long max, min;

    public void add(long num) {
        if (max == null) {
            max = num;
        } else {
            if (num > max) {
                max = num;
            }
        }
        if (min == null) {
            min = num;
        } else {
            if (num < min) {
                min = num;
            }
        }
        sum += num;
        if (((minHeap.size() + maxHeap.size()) & 1) == 0) {//偶数时,下个数字加入小顶堆
            if (!maxHeap.isEmpty() && maxHeap.peek() > num) {
                maxHeap.offer(num);
                num = maxHeap.poll();
            }
            minHeap.offer(num);
        } else {//奇数时，下一个数字放入大顶堆
            if (!minHeap.isEmpty() && minHeap.peek() < num) {
                minHeap.offer(num);
                num = minHeap.poll();
            }
            maxHeap.offer(num);
        }
    }

    public double getMedian() {
        if ((minHeap.size() + maxHeap.size()) == 0)
            throw new RuntimeException();
        double median;
        if ((minHeap.size() + maxHeap.size() & 1) == 0) {
            median = (maxHeap.peek() + minHeap.peek()) / 2.0;
        } else {
            median = minHeap.peek();
        }
        return median;
    }

    public double getAvg() {
        return sum / (minHeap.size() + maxHeap.size());
    }

    public long getMax() {
        return max;
    }

    public long getMin() {
        return min;
    }

    public void clear(){
        max=null;
        min=null;
        sum=0;
        minHeap.clear();
        maxHeap.clear();
    }

    @Override
    public String toString() {
        return "StreamNum{" +
                "avg=" + getAvg() +
                ", median=" + getMedian() +
                ", sum=" + sum +
                ", max=" + max +
                ", min=" + min +
                '}';
    }
}
