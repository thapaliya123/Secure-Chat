package com.example.anishthapaliya.securechatting;

public class Messages {
        private String message,type,from;
      //  private long time;
        //private boolean seen;
      public  Messages(){


      }

        public Messages(String message,String type/*,long time,boolean seen*/){
            this.message=message;
          //  this.seen=seen;
            this.type=type;
            //this.time=time;
            this.from=from;

        }
        public String getMessage(){

          return message;
        }
   //     public boolean getSeen(){
     //     return seen;
    //}
    public String getType(){
        return type;
    }

    public String getFrom(){
        return from;
    }
   // public long getTime(){
     //   return time;
    //}

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(){
          this.from=from;
    }

  //  public void setTime(long time) {
    //    this.time = time;
    //}

   // public void setSeen(boolean seen) {
        //this.seen = seen;
 //   }




}
