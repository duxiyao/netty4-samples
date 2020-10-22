package com;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.PriorityQueue;
import java.util.Comparator;

public class Test {

    //计算最大最小值，平均值，中位数
    static class StreamNum {
        PriorityQueue<Integer> minHeap = new PriorityQueue<Integer>(1); //小顶堆，默认容量为11
        PriorityQueue<Integer> maxHeap = new PriorityQueue<Integer>(1, new Comparator<Integer>() { //大顶堆，容量11
            public int compare(Integer i1, Integer i2) {
                return i2 - i1;//降序排列
            }
        });
        private double sum;
        private Integer max, min;

        public void add(int num) {
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

        public Integer getMax() {
            return max;
        }

        public Integer getMin() {
            return min;
        }
    }
//
//    static class CalcMedian {
//        private int size;
//        private Integer maxheap, minheap;
//
//        public void insert(int num) {
//            if ((size & 1) == 0) {//偶数时,下个数字加入小顶堆
//                if (minheap != null && minheap < num) {
//                    minheap = num;
//                } else {
//                    minheap = num;
//                }
//            } else {//奇数时，下一个数字放入大顶堆
//                if (maxheap != null && maxheap > num) {
//                    maxheap = num;
//                } else {
//                    maxheap = num;
//                }
//            }
//            size++;
//        }
//
//        public Double getMedian() {
//            if (size == 0)
//                throw new RuntimeException();
//            double median;
//            if ((size & 1) == 0) {
//                median = (maxheap + minheap) / 2.0;
//            } else {
//                median = minheap;
//            }
//            return median;
//        }
//    }

    private static int max(List<Integer> total) {
        Collections.sort(total);
        return total.get(total.size() - 1);
    }

    private static int min(List<Integer> total) {
        Collections.sort(total);
        return total.get(0);
    }

    private static double avg(List<Integer> total) {
        double sum = 0;
        for (Integer i : total) {
            sum += i;
        }
        return sum / total.size();
    }

    private static double median(List<Integer> total) {
        double j = 0;
        //集合排序
        Collections.sort(total);
        int size = total.size();
        if (size % 2 == 1) {
            j = total.get((size - 1) / 2);
        } else {
            //加0.0是为了把int转成double类型，否则除以2会算错
            j = (total.get(size / 2 - 1) + total.get(size / 2) + 0.0) / 2;
        }
        return j;
    }

    public static void main(String[] args) {
        StreamNum streamMedian = new StreamNum();
        List<Integer> total = new ArrayList<Integer>();
        for (int i = 0; i < 57; i++) {
            Random random = new Random();
            int data = random.nextInt(1000);
            total.add(data);
            streamMedian.add(data);
        }
//
//        total.add(8);
//        total.add(10);
//        total.add(4);
//        total.add(9);
//        total.add(3);
//        total.add(7);
//        total.add(1);
//
        total.forEach(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                System.out.print(integer + " ");
            }
        });
        System.out.println();


        System.out.println("数组生成完毕");
        long t1 = System.currentTimeMillis();
        double a = median(total);
        long t2 = System.currentTimeMillis();
        System.out.println((t2 - t1) + "  median:" + a);

        double b = streamMedian.getMedian();
        long t3 = System.currentTimeMillis();
        System.out.println((t3 - t2) + "  streamMedian median:" + b);

        System.out.println("最大值：" + max(total) + "------" + streamMedian.getMax());
        System.out.println("最小值：" + min(total) + "------" + streamMedian.getMin());
        System.out.println("平均值：" + avg(total) + "------" + streamMedian.getAvg());
    }
}
