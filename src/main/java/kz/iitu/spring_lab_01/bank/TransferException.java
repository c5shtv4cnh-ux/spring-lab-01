package kz.iitu.spring_lab_01.bank;

/** A checked exception used in task A to show the default rollback rules. */
public class TransferException extends Exception {
    public TransferException(String message) {
        super(message);
    }
}
