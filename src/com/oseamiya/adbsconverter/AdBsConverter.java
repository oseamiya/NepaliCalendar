package com.oseamiya.adbsconverter;

import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.common.ComponentCategory;

import android.content.Context;
import android.app.Activity;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import com.oseamiya.adbsconverter.Lookup;
import org.json.*;

@DesignerComponent(
    iconName = "",
    description = "",
    version = 1,
    category = ComponentCategory.EXTENSION,
    nonVisible = true
)
@UsesLibraries(libraries = "orgjson.jar")

@SimpleObject(external = true)

public class AdBsConverter extends AndroidNonvisibleComponent{
    private ComponentContainer container;
    private Context context;
    private Activity activity;
    private ArrayList<String> adEquivalentDatesForNewNepaliYear = new ArrayList<>();
    

    

    public AdBsConverter(ComponentContainer container){
        super(container.$form());
        this.context = container.$context();
        this.activity = (Activity) container.$context();

       
    }
    @SimpleEvent(description = "This event is triggered when AD is converted to BD")
    public void AdtoBSConverted(int day , int month , int year , int dayOfWeek){
        EventDispatcher.dispatchEvent(this , "AdtoBSConverted" , new Object[] {day , month , year ,dayOfWeek});

    }
    @SimpleEvent(description = "This event is triggered when BS is converted to AD")
    public void BsToAdConverted(int day, int month , int year , int dayOfWeek){
        EventDispatcher.dispatchEvent(this , "BsToAdConverted" , new Object[] {day , month , year ,dayOfWeek});

    }

    @SimpleFunction(description = "To convert BS to AD")
    public void BSToAD(int nepaliMonth , int nepaliDayOfMonth , int nepaliYear){
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
        BsToAdConverted((c1.get(Calendar.DAY_OF_MONTH)) , c1.get(Calendar.MONTH) , (c1.get(Calendar.YEAR)) , (c1.get(Calendar.DAY_OF_WEEK)));
    
               

    }
    private int getLookupIndex(int bsYear){
        return bsYear - Lookup.lookupNepaliYearStart;
    }

    @SimpleFunction(description = "To convert AD to BS")
    public void AdToBS(int month , int dayOfMonth , int year)  {
        String  monthInString = "";
        String dayOfMonthInString = "";
        if(String.valueOf(month).length() != 2){
             monthInString = "0" + String.valueOf(month);
        }else{
             monthInString = String.valueOf(month);
        }
        if(String.valueOf(dayOfMonth).length() != 2){
             dayOfMonthInString = "0" + String.valueOf(dayOfMonth);
        }else{
             dayOfMonthInString = String.valueOf(dayOfMonth);
        }
        String dateInFormatOfString = dayOfMonthInString +"-" + monthInString + "-" + String.valueOf(year);
        String[] adDateParts = dateInFormatOfString.split("-");
        DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate inputAdDate = LocalDate.parse(dateInFormatOfString, formatter);
        LocalDate lookupNearestAdDate = null;
        int equivalentNepaliYear = Lookup.lookupNepaliYearStart;
        Byte[] monthDay = null;
        for(int i = 0 ; i < Lookup.adEquivalentDatesForNewNepaliYear.size() ; i++){
            String[] adEquivalentDateForNewNepaliYear = Lookup.adEquivalentDatesForNewNepaliYear.get(i).split("-");
            if(adEquivalentDateForNewNepaliYear[2].equals(adDateParts[2])){
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
                lookupNearestAdDate = LocalDate.parse(Lookup.adEquivalentDatesForNewNepaliYear.get(i) , formatter1);
                monthDay = Lookup.numberOfDaysInNepaliMonth.get(i + Lookup.lookupNepaliYearStart);
                equivalentNepaliYear += i;
        
                if(inputAdDate.isBefore(lookupNearestAdDate)){
                    
                    lookupNearestAdDate = LocalDate.parse(Lookup.adEquivalentDatesForNewNepaliYear.get(i-1) , formatter1);
                    equivalentNepaliYear -= 1;
                    monthDay = Lookup.numberOfDaysInNepaliMonth.get(i + Lookup.lookupNepaliYearStart - 1);
                }
        
            }
        
        
        }
        assert lookupNearestAdDate != null;
        long difference = ChronoUnit.DAYS.between(lookupNearestAdDate, inputAdDate);
        int nepMonth = 0;
        int nepDay = 1;
        int daysInMonth;
        while(difference != 0){
            if(difference >= 0){
                daysInMonth = monthDay[nepMonth];
                nepDay++;
                if(nepDay > daysInMonth){
                    nepMonth++;
                    nepDay = 1;
                }
                if(nepMonth >= 12){
                    equivalentNepaliYear++;
                    nepMonth = 0;
                }
                difference--;
            }
        
        }
        
        nepMonth += 1;

        LocalDate forweek = LocalDate.of(year, month, dayOfMonth);
        DayOfWeek dw = forweek.getDayOfWeek();

        AdtoBSConverted(nepDay  , nepMonth , equivalentNepaliYear , dw.getValue());
        
        
        
    }
    @SimpleEvent(description = "It is fired when json data of year is gotten")
    public void GotJson(String json){
        EventDispatcher.dispatchEvent(this, "GotJson" , new Object[]{json});

    }

    @SimpleFunction(description = "Get Json data of the year. It always accept nepali date")
    public  void GetJson(int year){
        final String url = "https://raw.githubusercontent.com/oseamiya/NepaliCalendar/main/api/" + String.valueOf(year) + ".json";
        AsynchUtil.runAsynchronously(new Runnable(){
            @Override
            public void run(){
                BufferedReader in;
                try{
                    in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                    String inputLine;
                    final StringBuilder result = new StringBuilder();

                    while((inputLine = in.readLine()) != null)
                        result.append(inputLine);
                        in.close();
                    
                    activity.runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            GotJson(result.toString());
                        }
                    });

                    
                } catch(IOException e){
                    e.printStackTrace();
                    
                }
            }
        });

    }
    
    @SimpleEvent
    public void JsonFormatted(String json , String tithi , String event , String isHoliday , String isSpecialDay){
        EventDispatcher.dispatchEvent(this, "JsonFormatted" , new Object[] {json , tithi , event , isHoliday , isSpecialDay});

    }

    @SimpleFunction(description = "Format the json to get tithi, if the day is holiday,etc ")
    public  YailList FormatJson(String json , String month , int day){
        String actMonth = "";
        if(month == "1"){
            actMonth = "Baishakh";
        }else if(month == "2"){
            actMonth = "Jestha";
        }else if(month == "3"){
            actMonth = "Ashadh";
        }else if(month == "4"){
            actMonth = "Shrawan";
        }else if(month == "5"){
            actMonth = "Bhadra";
        }else if(month == "6"){
            actMonth = "Ashwin";
        }else if(month == "7"){
            actMonth = "Kartik";
        }else if(month == "8"){
            actMonth = "Mangsir";
        }else if(month == "9"){
            actMonth = "Poush";
        }else if(month == "10"){
            actMonth = "Magh";
        }else if(month == "11"){
            actMonth = "Falgun";
        }else if(month == "12"){
            actMonth = "Chaitra";
        }else{
            String firstLetter = month.substring(0, 1);
            String remainingLetter = month.substring(1);
            // to have first letterr capital
            actMonth = firstLetter.toUpperCase() + remainingLetter;

        }
        JSONObject jsonObject = new JSONObject(json);
        JSONArray arr = jsonObject.getJSONArray(actMonth);
        List tithiList = new ArrayList<String>();

        for(int i = 0 ; i<arr.length() ; i++){
            String tithi = arr.getJSONObject(i).getString("tithi");
            tithiList.add(tithi);
        }
        List nepDayList = new ArrayList<String>();
        for(int i =0 ; i<arr.length() ; i++){
            String nepDay = arr.getJSONObject(i).getString("np");
            nepDayList.add(nepDay);
        }
        List eventList = new ArrayList<String>();
        for(int i =0 ; i<arr.length() ; i++){
            String events = arr.getJSONObject(i).getString("event");
            eventList.add(events);
        }
        List isHolidayList = new ArrayList<String>();
        for(int i= 0 ; i<arr.length() ; i++){
            String isHolidays = arr.getJSONObject(i).getString("holiday");
            isHolidayList.add(isHolidays);
        }
        List isSpecialDayList = new ArrayList<String>();
        for(int i= 0 ; i<arr.length() ; i++){
            String isSpecialDays = arr.getJSONObject(i).getString("specialday");
            isSpecialDayList.add(isSpecialDays);
        }

        int dayIndex = nepDayList.indexOf(day);
        JsonFormatted(json, tithiList.get(dayIndex).toString(), eventList.get(dayIndex).toString(), isHolidayList.get(dayIndex).toString(), isSpecialDayList.get(dayIndex).toString());

        
    
       
        
    }
      

}


