package eu.pmsoft.sam.idgenerator;

public class LongLongIdGenerator {

    private final IdFactorialGenerator maskGenerator = IdFactorialGenerator.createGenerator();
    private long linearCounter =  Long.MIN_VALUE;
    private long currentMask = maskGenerator.nextId();

    private LongLongIdGenerator(){
    }

    public static synchronized LongLongIdGenerator createGenerator() {
        return new LongLongIdGenerator();
    }

    public LongLongID getNextID() {
        long linear = linearCounter++;
        if( linear == Long.MAX_VALUE) {
            currentMask = maskGenerator.nextId();
            linearCounter =  Long.MIN_VALUE;
        }
        return new LongLongID(currentMask,linear);
    }
}
