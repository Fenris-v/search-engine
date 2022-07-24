package main.exceptions;

public class DomainNotInListException extends Exception {
    @Override
    public String getMessage() {
        return "Данная страница находится за пределами сайтов, указанных в конфигурационном файле";
    }
}
