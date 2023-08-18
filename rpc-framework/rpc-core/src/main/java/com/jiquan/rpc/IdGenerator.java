package com.jiquan.rpc;

import com.jiquan.utils.DateUtil;

import java.util.concurrent.atomic.LongAdder;

/**
 * Snowflake Algorithm -- No snowflake in the world is the same
 * computer room number (data center) 5bit 32
 * machine number 5bit 32
 * Timestamp (long 1970-1-1)
 * The time originally represented by 64 bits must be reduced (64), and a relatively recent time can be freely selected
 * For example, the date our company was founded The same time of the same machine number in the same computer room can require multiple ids because of the large amount of concurrency
 * serial number 12bit
 * 5+5+42+12 = 64
 * @author ZHONG Jiquan
 * @year 2023
 */
public class IdGenerator {


	// start timestamp
	public static final long START_STAMP = DateUtil.get("2022-1-1").getTime();

	public static final long DATA_CENTER_BIT = 5L;
	public static final long MACHINE_BIT = 5L;
	public static final long SEQUENCE_BIT = 12L;

	// max value
	public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT); // Math.pow(2, 5) - 1
	public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT); // Math.pow(2, 5) - 1
	public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT); // Math.pow(2, 5) - 1

	// offset of each part
	public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
	public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
	public static final long MACHINE_LEFT = SEQUENCE_BIT;

	private long dataCenterId;
	private long machineId;
	private LongAdder sequenceId = new LongAdder();

	private long lastTimeStamp = -1L;

	public IdGenerator(long dataCenterId, long machineId) {
		if(dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX){
			throw new IllegalArgumentException("The id of data center or machine is invalid.");
		}
		this.dataCenterId = dataCenterId;
		this.machineId = machineId;
	}

	public long getId(){
		long currentTime = System.currentTimeMillis();
		long timeStamp = currentTime - START_STAMP;

		// if there's the clock back ?
		if(timeStamp < lastTimeStamp){
			throw new RuntimeException("There is a clock back on the server");
		}

		if(timeStamp == lastTimeStamp){
			sequenceId.increment();
			if(sequenceId.sum() >= SEQUENCE_MAX){
				timeStamp = getNextTimeStamp();
				sequenceId.reset();
			}
		}else {
			sequenceId.reset();
		}

		lastTimeStamp = timeStamp;
		long sequence = sequenceId.sum();
		return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT
				| machineId << MACHINE_LEFT | sequence;
	}

	private long getNextTimeStamp() {
		long current = System.currentTimeMillis() - START_STAMP;
		while (current == lastTimeStamp){
			current = System.currentTimeMillis() - START_STAMP;
		}
		return current;
	}

	public static void main(String[] args) {
		IdGenerator idGenerator = new IdGenerator(1,2);
		for (int i = 0; i < 1000; i++) {
			new Thread(() -> System.out.println(idGenerator.getId())).start();
		}
	}
}
