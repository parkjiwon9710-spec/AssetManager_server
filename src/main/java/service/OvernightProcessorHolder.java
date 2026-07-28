package service;


public class OvernightProcessorHolder {


    private static OvernightProcessor instance;



    public static void init(
            OvernightProcessor processor
    ){

        instance = processor;

    }



    public static OvernightProcessor get(){

        return instance;

    }

}