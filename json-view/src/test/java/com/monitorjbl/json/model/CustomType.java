package com.monitorjbl.json.model;

public class CustomType
{
    private long sid=0l;
    private String name="name";
    
    
    public CustomType(long sid,String name){
        this.setSid( sid );
        this.setName( name );
    }
    
    public long getSid()
    {
        return sid;
    }

    public void setSid( long sid )
    {
        this.sid = sid;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    } 
    
    
    public boolean equals(Object obj){
        if(obj instanceof CustomType){
            CustomType c=(CustomType)obj;
            return c.name.equals( name ) && c.sid==sid;
        }else{
            return false;
        }
    }
    
}
