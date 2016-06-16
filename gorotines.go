package main

import (
	"strconv"
	"time"
	"fmt"
	"runtime"
)

type Sum []int

func (s Sum) Calculate(count, start, end int, flag string, ch chan int) {
	cal := 0

	for i := start; i <= end; i++ {
		for j := 1; j <= 3000000; j++ {
		}
		cal += i
	}

	s[count] = cal
	fmt.Println("flag :", flag, ".")
	ch <- count
}

const RANGE = 10000

func (s Sum) LetsGo(NCPU int) {

	var ch = make(chan int)

	runtime.GOMAXPROCS(NCPU)
	for i := 0; i < NCPU; i++ {
		go s.Calculate(i, (RANGE / NCPU) * i + 1, (RANGE / NCPU) * (i + 1), strconv.Itoa(i + 1), ch)
	}

	for i := 0; i < NCPU; i++ {
		<-ch
	}
}

func main() {
	var s Sum = make([]int, runtime.NumCPU())
	var sum int = 0
	var startTime = time.Now()

	s.LetsGo(runtime.NumCPU())

	for _, v := range s {
		sum += v
	}

	fmt.Println("总数为：", sum, "；所花时间为：",
		(time.Now().Sub(startTime)), "秒。")
}