package com.oseamiya.adbsconverter;

import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.common.ComponentCategory;

import android.content.Context;
import android.app.Activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import com.oseamiya.adbsconverter.Lookup;

@DesignerComponent(
    iconName = "",
    description = "",
    version = 1,
    category = ComponentCategory.EXTENSION,
    nonVisible = true
)
@UsesLibraries(libraries = "slf4j.jar")

@SimpleObject(external = true)

public class AdBsConverter extends AndroidNonvisibleComponent{
    private ComponentContainer container;
    private Context context;
    private Activity activity;
    private String defaultFormat = "ddMMYYYY";
    private String format;
    private char separator;
    private static Logger logger = LoggerFactory.getLogger(AdBsConverter.class);



    public AdBsConverter(ComponentContainer container){
        super(container.$form());
        this.context = container.$context();
        this.activity = (Activity) container.$context();
    }

    @SimpleFunction(description = "To convert BS to AD")
    public String BSToAD(int nepaliMonth , int nepaliDayOfMonth , int nepaliYear){
        int numberOfDaysPassed = nepaliDayOfMonth - 1;
        for(int i=0 ; i<= nepaliMonth - 2 ; i++){
            numberOfDaysPassed += Lookup.numberOfDaysInNepaliMonth.get(nepaliYear)[i];
        }
        String dateFormat = "dd-MMM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        Calendar c1 = Calendar.getInstance();
        try {
            c1.setTime(sdf.parse(Lookup.adEquivalentDatesForNewNepaliYear.get(getLookupIndex(nepaliYear))));
        } catch(ParseException e ){
            e.printStackTrace();
        }
        c1.add(Calendar.DATE , numberOfDaysPassed);

        String result = String.valueOf(c1.getTime());
        return result;
    
               

    }
    private int getLookupIndex(int bsYear){
        logger.debug("lookup index {} ", (bsYear - Lookup.lookupNepaliYearStart));
        return bsYear - Lookup.lookupNepaliYearStart;

    }
}


