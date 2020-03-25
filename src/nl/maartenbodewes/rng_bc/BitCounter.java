package nl.maartenbodewes.rng_bc;

/**
 * Interface for classes that want to keep a bit count that can be retrieved and reset.
 * 
 * @author maartenb
 */
interface BitCounter {
    /**
     * Retrieves the bit count.
     * 
     * @return the bit count, a positive value or zero
     */
    long getBitCount();
    
    /**
     * Resets the bit count to zero while not touching any other state.
     */
    public void resetBitCount();
}
