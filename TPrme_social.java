import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.DateFormat;

import java.util.concurrent.*;
class CheckIn{
	int user_id;
	double latitude,longitude;
    long time;
    int timestamp;
	int point_id;
	void debg(){
		System.out.print(point_id+user_id+latitude+longitude+"时间是"+time);

	}
}
class Edge{
	int user_id;
	int friend_id;
	double  social_similarity;
}
public class TPrme_social {

	/**
	 * @param args
	 */

	static int MAX_USER=3300;
	static int MAX_POINT=50000;
	static int TIME_PERIODS=8;
	static double mu=0.01;
    static double P_WEIGHT=1;
	static double alpha=0.1;
	static double beta=0.8;
	static int NUM_DIM=50;
	static int NUM_ITER=1000; 
	static double LEARNING_RATE=0.003;
    static double LAMBDA=0.03;
    static int THRESHOLD=6*3600;
	static Vector<CheckIn> checkins=new Vector();
	static Vector<Edge> edges=new Vector();
	static  Map<Integer,Integer >  raw_id2user_id=new HashMap();
	static  Map<String,Integer >  raw_id2point_id=new HashMap();
	static  Map<Integer,Integer>  new_raw_id2user_id=new HashMap();
	static  int user_id2raw_id[]=new int[MAX_USER];
	static  int new_user_id2raw_id[]=new int[MAX_USER];
	static  String point_id2raw_id[]=new String[MAX_POINT];
	static  Vector<Integer> user_checkins[]=new Vector[MAX_USER];
	static  Vector<Integer> user_friends[]=new Vector[MAX_USER];
	static  Vector<Integer> user_time_checkins[][]=new Vector[MAX_USER][TIME_PERIODS];
	static Map<Integer,Integer> visited[]=new HashMap[MAX_USER];
	static Set<Integer> visit;
	static Map<Integer,Integer> temp_visited[]=new HashMap[MAX_USER];
	static Vector<Edge> nz_similarity_friends[]=new Vector[MAX_USER];
	static double  XS_L[][]=new double [MAX_POINT ][NUM_DIM];
	static double  XP_L[][]=new double [MAX_POINT ][NUM_DIM];
	static double  XP_U[][]=new double [MAX_USER  ][NUM_DIM];
	static double  XT_T[][]=new double [TIME_PERIODS][NUM_DIM];
	static double  XT_L[][]=new double [MAX_POINT][NUM_DIM];
     static int total_user=0;
	 static int total_point=0;
     static int total_checkin=0; 
     static int total_edge=0;
     static int total_nz_similarity=0;
     public static int  getMonthofDate(Date date){

 	    Calendar calendar = Calendar.getInstance(); 
 	  
		calendar.setTime(date); 
   	    int intMonth=calendar.get(Calendar.MONTH) +1;
     	return intMonth;
     }
     public static int  getHourofDate(Date date){

 	    Calendar calendar = Calendar.getInstance(); 
 	    calendar.setTime(date); 
     	int intHour=calendar.get(Calendar.HOUR_OF_DAY);
    	return intHour;
     }
     public static int  getDayofMonth(Date date){

   	    Calendar calendar = Calendar.getInstance(); 
   	    calendar.setTime(date); 
     	int intDay=calendar.get(Calendar.DAY_OF_MONTH);
     	return intDay;
     }
     public static int  getMinute(Date date){

   	    Calendar calendar = Calendar.getInstance(); 
   	    calendar.setTime(date); 
     	int intMinute=calendar.get(Calendar.MINUTE);
     	return intMinute;
     }
     public static int  getYear(Date date){

   	    Calendar calendar = Calendar.getInstance(); 
   	    calendar.setTime(date); 
     	int intYear=calendar.get(Calendar.YEAR);
     	return intYear;
     }
     public static int getWeek(Date date){
    	 Calendar calendar = Calendar.getInstance(); 
    	 calendar.setTime(date); 
      	int intWeek=calendar.get(Calendar.DAY_OF_WEEK)-1;
      	return intWeek;
     }
     int data2timestamp(Date date){
    	 int month=getMonthofDate(date);
    	 if(month==12){
    		int a=0;
    	 }
    	 int hour=getHourofDate(date);
    	 int intWeek=getWeek(date);
    	 int hour_id=0;
    	 int week_id=0;
    	 if(hour>=3&&hour<6){
    		 hour_id=0;
    	 }else if(hour>=6&&hour<11){
    		 hour_id=1;
    	 }else if(hour>=11&&hour<15){
    		 hour_id=2;
    	 }else if((hour>=15&&hour<=24)||(hour>=0&&hour<3)){
    		 hour_id=3;
    	 }
    	 if(intWeek>0&&intWeek<=5){
    		 week_id=0;
    	 }else{
    		 week_id=1;
    	 }
    	 int timestamp=week_id*4+hour_id;
         return  timestamp;
    	 
     }
     long date2time (Date date){
    	 int year=getYear(date);
    	 int month=getMonthofDate(date);
    	 int day=getDayofMonth(date);
    	 int hour=getHourofDate(date);
    	 int minute=getMinute(date);
    	 //int time=(year-1990)*365*24*3600+month*30*24*3600+day*24*3600+hour*3600+minute*60;
    	 long time=date.getTime()/1000;
    	 return time;
     }
    double guassrand(){
        double v1 = 0,v2 = 0,s = 0;
    	int phase=0;
    	double X;
    	Random random=new Random();
    	if(phase==0){
    	    while(s>=1||s==0)
    	         {
    	    	   double u1=random.nextDouble();
    	    	   double u2=random.nextDouble();
    	    	   v1=2*u1-1;
    	    	   v2=2*u2-1;
    	    	   s=v1*v1+v2*v2;
    	         }
    	    X=v1*Math.sqrt(-2*Math.log(s)/s);
    	}else{
    		X=v2*Math.sqrt(-2*Math.log(s)/s);
    	  
    	}
    	  phase=1-phase;
    	  return X;
    }
    void load_social_data(String file_name){
        int total_user1=total_user;
      	 File file=new File(file_name);
      	 for(int i=0;i<MAX_USER;i++){
      	   user_friends[i]=new Vector();
      	 }
      	 BufferedReader br;
           String temp=null;
           try {
          	       br = new BufferedReader(new FileReader(file));
   			       temp=br.readLine();
   			       while(temp!=null){
   			       int id1;
   			       int id2;
   			       Edge edge=new Edge();
   				   String []catalog=temp.split(" ");
   				   id1=Integer.parseInt(catalog[0]);
   				   id2=Integer.parseInt(catalog[1]);
   				   if(id1==12)
   					   System.out.print("pause");
   				   if(id2==26)
   					   System.out.print("pause");
   				   if(!raw_id2user_id.containsKey(id1)){ 
   					   new_user_id2raw_id[total_user1]=id1;
   					   new_raw_id2user_id.put(id1, total_user1++);
   				      
   				   }
   				   if(!raw_id2user_id.containsKey(id2)){
   					   new_user_id2raw_id[total_user1]=id2;
   					   new_raw_id2user_id.put(id2, total_user1++);
   					   
   				   }
   				   if(raw_id2user_id.containsKey(id1)){
   					   edge.user_id=raw_id2user_id.get(id1);
   					   if(raw_id2user_id.containsKey(id2))
   	                   edge.friend_id=raw_id2user_id.get(id2);
   					   else{
   						   edge.friend_id=new_raw_id2user_id.get(id2);
   					   }
   				   }
                      if(raw_id2user_id.containsKey(id1))
   				   user_friends[raw_id2user_id.get(id1)].add(edges.size());
   				   edges.add(edge);
   				   total_edge++;				
   				   temp=br.readLine();
   			       }
   		 } catch (IOException e) {
   			// TODO Auto-generated catch block
   			   e.printStackTrace();
   		   }
      	 
      }
     void load_data(String file_name){
    	 total_point=0;total_user=0;total_checkin=0;
    	 for(int i=0;i<MAX_USER;i++){
    		 visited[i]=new HashMap();
    	 }
    	 for(int i=0;i<MAX_USER;i++){
    	     user_checkins[i]=new Vector();
    	 }
    	 File file=new File(file_name);
    	 BufferedReader br;
         String temp=null;
         try {
        	       br = new BufferedReader(new FileReader(file));
			       temp=br.readLine();
			       while(temp!=null){
			       int id1;
			       String id2;
			       CheckIn checkin=new CheckIn();
				   String []catalog=temp.split(" ");
				   id1=Integer.parseInt(catalog[0]);
				   id2=catalog[4];
				   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				    String s=catalog[1];
				    String []timesplit=s.split("T");
				    s=timesplit[0]+" "+timesplit[1];
				    String []finalsplit=s.split("Z");
			      
				    s=finalsplit[0];
				    
					Date date = null;
					try {
						date = (Date) sdf.parse(s);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			
                    checkin.time=date2time(date);
                    
                    checkin.timestamp=data2timestamp(date);

				   if(!raw_id2user_id.containsKey(id1)){
					    user_id2raw_id[total_user]=id1;
					    raw_id2user_id.put(id1, total_user++);
			             }
				   if(!raw_id2point_id.containsKey(id2))
				        {
					       point_id2raw_id[total_point]=id2;
					       raw_id2point_id.put(id2, total_point++);
				        }
				    checkin.user_id=raw_id2user_id.get(id1);
				    checkin.point_id=raw_id2point_id.get(id2);
				    if(!visited[checkin.user_id].containsKey(checkin.point_id))
				    	visited[checkin.user_id].put(checkin.point_id, 0);
				    int a=visited[checkin.user_id].get(checkin.point_id);
				    
				    visited[checkin.user_id].put(checkin.point_id,a++);
				    user_checkins[checkin.user_id].add(checkins.size());
				    checkins.add(checkin);
				    total_checkin++;
				    temp=br.readLine();
			       }
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			   e.printStackTrace();
		   }
    	 
     }
     
    void init_theta(){
    	Random guassrand=new Random();
    	for(int i=0;i<total_user;i++)
    		for(int j=0;j<NUM_DIM;j++){
    			XP_U[i][j]=0.1*guassrand();   			       
    		}
    	for(int i=0;i<total_point;i++){
    		for(int j=0;j<NUM_DIM;j++){
    			XP_L[i][j]=0.1*guassrand();
    		}
    		for(int j=0;j<NUM_DIM;j++){
    			XS_L[i][j]=0.1*guassrand();
    		}
    		for(int j=0;j<NUM_DIM;j++){
    			XT_L[i][j]=0.1*guassrand();
    		}
    	}
    	for(int i=0;i<TIME_PERIODS;i++){
    		for(int j=0;j<NUM_DIM;j++){
    			XT_T[i][j]=0.1*guassrand();
    		}
    	}

    }
     long  time_diff(int i,int j){
    	 
	 return Math.abs(checkins.get(i).time-checkins.get(j).time );
    	
    }
    int random_choice_unvisited(int user_id){
    	Random random_unvisited=new Random();
    	int k=random_unvisited.nextInt(total_point);
    	for(int i=k;i<total_point+k;i++){
    		int pid=i%total_point;
    		//int a=visited[user_id].get(pid);
    		if(!visited[user_id].containsKey(pid)||visited[user_id].get(pid)==0)
    			return pid;
    	}
    	return 0;
    			
    }
     double sigmod(double x){
    	 return 1.0/(1.0+Math.exp(-x));
     }
     double dis(double A[],double B[]){
    	 double res=0;
    	 int dim=NUM_DIM;
    	 for(int i=0;i<dim;i++){
    		 res+=(A[i]-B[i])*(A[i]-B[i]);
    	 }
    	 return res;
     }
     double calc_dp(int u,int l){
    	 return dis(XP_L[l],XP_U[u]);
     }
     double calc_ds(int la,int lb){
    	 return dis(XS_L[la],XS_L[lb]);
     }
     double calc_dt(int t,int l){
    	 return dis(XT_T[t],XT_L[l]);
     }
     double calc_d(int u,int lc,int li,int t,double alpha,double beta){
    	 return alpha*calc_dp(u,li)+beta*calc_ds(lc,li)+(1-alpha-beta)*calc_dt(t,li);
     }
     double calc_z(int u,int lc,int li,int lj,int t,double flag1,double flag2){
    	 double alpha=flag1;
    	 double beta=flag2;
		 return calc_d(u,lc,lj,t,alpha,beta)-calc_d(u,lc,li,t,alpha,beta);
    	 
     }
     double calc_new_theta(double theta,double k,double d){
    	 return theta+LEARNING_RATE*(k*d-2*LAMBDA*theta);
     }
     void update(int id_u,int lc,int li,int id_lj){
    	    int id_lc=checkins.get(lc).point_id;
    	    int id_li=checkins.get(li).point_id;
    	    double dp_ul_lj=0;
    	    int  t;
    	    for(t=0;t<NUM_DIM;t++){
    	        dp_ul_lj=dp_ul_lj+(XP_L[id_lj][t]-XP_U[id_u][t])*(XP_L[id_lj][t]-XP_U[id_u][t]);
    	    }
    	    double dp_ul_li=0;
    	    for(t=0;t<NUM_DIM;t++){
    	        dp_ul_li=dp_ul_li+(XP_L[id_li][t]-XP_U[id_u][t])*(XP_L[id_li][t]-XP_U[id_u][t]);
    	    }
    	    double ds_lc_lj=0;
    	    for(t=0;t<NUM_DIM;t++){
    	        ds_lc_lj=ds_lc_lj+(XS_L[id_lc][t]-XS_L[id_lj][t])*(XS_L[id_lc][t]-XS_L[id_lj][t]);
    	    }
    	     double ds_lc_li=0;
    	     for(t=0;t<NUM_DIM;t++){
    	        ds_lc_li=ds_lc_li+(XS_L[id_lc][t]-XS_L[id_li][t])*(XS_L[id_lc][t]-XS_L[id_li][t]);
    	     }
    	     double deltaZ=P_WEIGHT*(dp_ul_lj-dp_ul_li)+(1-P_WEIGHT)*(ds_lc_lj-ds_lc_li);
    	     deltaZ=1/(1+Math.exp(-deltaZ));
    	     if(time_diff(lc,li)<THRESHOLD) {
    	        for( t=0;t<NUM_DIM;t++){
    	          double tmp = XS_L[id_lc][t];
    								XS_L[id_lc][t] = XS_L[id_lc][t] + 2 * LEARNING_RATE *
    									( (1-deltaZ) * (1-P_WEIGHT) * (XS_L[id_li][t]-XS_L[id_lj][t]) - LAMBDA* XS_L[id_lc][t] );

    								XS_L[id_li][t] = XS_L[id_li][t] +  2 * LEARNING_RATE *
    									( (1-deltaZ) * (1-P_WEIGHT) * (tmp-XS_L[id_li][t]) - LAMBDA * XS_L[id_li][t] );

    								XS_L[id_lj][t] = XS_L[id_lj][t] -  2 * LEARNING_RATE *
    									( (1-deltaZ) * (1-P_WEIGHT) * (tmp-XS_L[id_lj][t]) + LAMBDA * XS_L[id_lj][t] );
    	        }
    	        for (t = 0; t < NUM_DIM; t++)
    							{
    								double tmp = XP_U[id_u][t];
    								XP_U[id_u][t] = XP_U[id_u][t] + 2 * LEARNING_RATE *
    									( (1-deltaZ) * P_WEIGHT * (XP_L[id_li][t]-XP_L[id_lj][t]) - LAMBDA * XS_L[id_u][t] );

    								XP_L[id_li][t] = XP_L[id_li][t] +  2 * LEARNING_RATE *
    									( (1-deltaZ) * P_WEIGHT * (tmp-XP_L[id_li][t]) - LAMBDA* XP_L[id_li][t] );

    								XP_L[id_lj][t] = XP_L[id_lj][t] -  2 * LEARNING_RATE *
    									( (1-deltaZ) * P_WEIGHT * (tmp-XP_L[id_lj][t]) + LAMBDA * XP_L[id_lj][t] );
    							}
    	     }else{
    	        for( t=0;t<NUM_DIM;t++){
    	           double tmp = XP_U[id_u][t];
    								XP_U[id_u][t] = XP_U[id_u][t] + 2 * LEARNING_RATE*
    									( (1-deltaZ) * P_WEIGHT * (XP_L[id_u][t]-XP_L[id_lj][t]) - LAMBDA * XP_U[id_u][t] );

    								XP_L[id_li][t] = XP_L[id_li][t] +  2 * LEARNING_RATE *
    									( (1-deltaZ) * P_WEIGHT * (tmp-XP_L[id_li][t]) - LAMBDA * XP_L[id_li][t] );

    								XP_L[id_lj][t] = XP_L[id_lj][t] -  2 * LEARNING_RATE*
    									( (1-deltaZ) * P_WEIGHT * (tmp-XP_L[id_lj][t]) + LAMBDA * XP_L[id_lj][t] );
    	        }
    	     }

    	}
     double computing_social_similarity(int su1[],int su2[]){
         double result=0;
           double sim_numerator=0;
          double su1_denominator=0;
          double su2_denominator=0;
          for(int i=0;i<su1.length;i++){
       	  sim_numerator+=su1[i]*su2[i];
       	  su1_denominator+=su1[i];
       	  su2_denominator+=su2[i];
          }
          if(Math.sqrt(su1_denominator)*Math.sqrt(su2_denominator)>0)
       	result=sim_numerator/(Math.sqrt(su1_denominator)*Math.sqrt(su2_denominator));
          return result;
        }
       double similarity(Vector<Integer> user_friend1,Vector<Integer> user_friend2){
       	double result=0;
       	Vector<Integer>  Su1=new Vector();
       	Vector<Integer>  Su2=new Vector();
    	    System.out.print("Su1");
       	for(int id=0;id<user_friend1.size();id++){
       		//System.out.print(edges.get(user_friend1.get(id)).friend_id+" ");
       		//if(user_friend1.get(id)!=13672)
       		/*if(raw_id2user_id.containsValue(edges.get(user_friend1.get(id)).friend_id)){
       		    System.out.print(user_id2raw_id[edges.get(user_friend1.get(id)).friend_id]+" ");
       		}else{
       			System.out.print(new_user_id2raw_id[edges.get(user_friend1.get(id)).friend_id]+" ");
       		}*/
       		Su1.add(edges.get(user_friend1.get(id)).friend_id);
       	}
       	System.out.print("Su2");
       	for(int id=0;id<user_friend2.size();id++){
       		//if(user_friend2.get(id)!=13672)
       		/*if(raw_id2user_id.containsValue(edges.get(user_friend2.get(id)).friend_id)){
       		   System.out.print(user_id2raw_id[edges.get(user_friend2.get(id)).friend_id]+" ");
       		}else{
       		   System.out.print(new_user_id2raw_id[edges.get(user_friend2.get(id)).friend_id]+" ");
       		}*/
       		Su2.add(edges.get(user_friend2.get(id)).friend_id);
       	}
       	Vector<Integer> Su_merge=new Vector();
       	Su_merge.addAll(Su1);
       	Su_merge.addAll(Su2);
       	Vector<Integer> Su_merge_withoutdup=new Vector(new HashSet(Su_merge));
       	int su1[]=new int[Su_merge_withoutdup.size()];
       	int su2[]=new int[Su_merge_withoutdup.size()];
       	for(int i=0;i<Su_merge_withoutdup.size();i++){
       	 int temp=Su_merge_withoutdup.get(i);
       	 if(Su1.contains(temp)){
       		 su1[i]=1;
       	 }else{
       		 su1[i]=0;
       	 }
       	 if(Su2.contains(temp)){
       		 su2[i]=1;
       	 }else{
       		 su2[i]=0;
       	 }
       	}
           result=computing_social_similarity(su1,su2);
           if(result>0)
           	total_nz_similarity++;
       	return result;
       	
       }
       void store_similarity(){
       	for(int i=0;i<total_user;i++){
       		nz_similarity_friends[i]=new Vector();
       	} 
       	for(int ui=0;ui<total_user;ui++){
       	    if(user_id2raw_id[ui]==0)
       		   System.out.println("entry");
       	    if(user_id2raw_id[ui]==12)
       	    	System.out.println("entry");
       	   //  if(raw_id2user_id.containsKey(0))
       	    	// System.out.print("contain");
       		for(int i=0;i<user_friends[ui].size();i++){
            /*   if(user_friends[edges.get(user_friends[ui].get(i)).friend_id].size()!=0)
               	System.out.print("entry");
               if(raw_id2user_id.containsKey(user_id2raw_id[ui]))
               		System.out.print("entry");
               if(raw_id2user_id.containsKey(user_id2raw_id[edges.get(user_friends[ui].get(i)).friend_id]))
               	    System.out.print("entry");*/
       	   // System.out.println(user_id2raw_id[edges.get(user_friends[ui].get(i)).friend_id]+"    "+user_id2raw_id[ui]);
       		if(user_friends[edges.get(user_friends[ui].get(i)).friend_id].size()!=0&&raw_id2user_id.containsKey(user_id2raw_id[ui])&&raw_id2user_id.containsKey(user_id2raw_id[edges.get(user_friends[ui].get(i)).friend_id]))
          		   edges.get(user_friends[ui].get(i)).social_similarity=similarity(user_friends[ui],user_friends[edges.get(user_friends[ui].get(i)).friend_id]);
       		   if(edges.get(user_friends[ui].get(i)).social_similarity>0)
       		      nz_similarity_friends[ui].add(edges.get(user_friends[ui].get(i)));
       		}
       	}
       }
       double[] computing_social_regulation(int user_id){
       	double social_regulation=0;
       	double sum_similarity=0;
       	double S_XP_U[]=new double[NUM_DIM];
       	for(int id=0;id<nz_similarity_friends[user_id].size();id++){
       		sum_similarity+=nz_similarity_friends[user_id].get(id).social_similarity;
       		for(int dim=0;dim<NUM_DIM;dim++){
       			S_XP_U[dim]+=nz_similarity_friends[user_id].get(id).social_similarity*XP_U[nz_similarity_friends[user_id].get(id).friend_id][dim];
       		}
       	}
       	if(sum_similarity>0)
       	for(int dim=0;dim<NUM_DIM;dim++){
       		S_XP_U[dim]=XP_U[user_id][dim]-S_XP_U[dim]/sum_similarity;
       		
       	}
       	return S_XP_U;  
       }
    void update_theta(int id_u,int lc,int li,int id_lj){
    	int id_lc=checkins.get(lc).point_id;
    	int id_li=checkins.get(li).point_id;
    	int id_t=checkins.get(lc).timestamp;
    	if(time_diff(lc,li)<THRESHOLD){
    		double k=1-sigmod(calc_z(id_u,id_lc,id_li,id_lj,id_t,alpha,beta));
    		double S_XP_U[]=computing_social_regulation(id_u);
    		for(int i=0;i<NUM_DIM;i++){
    			double d_PU=2.0*alpha*(XP_L[id_li][i]-XP_L[id_lj][i])-2*mu*S_XP_U[i];
    			double d_PLI=2.0*alpha*(XP_U[id_u][i]-XP_L[id_li][i]);
    			double d_PLJ=-2.0*alpha*(XP_U[id_u][i]-XP_L[id_lj][i]);
    			double d_SLC=2.0*beta*(XS_L[id_li][i]-XS_L[id_lj][i]);
    		    double d_SLI=2.0*beta*(XS_L[id_lc][i]-XS_L[id_li][i]);
    		    double d_SLJ=-2.0*beta*(XS_L[id_lc][i]-XS_L[id_lj][i]);
    		    double d_TT=2.0*(1-alpha-beta)*(XT_L[id_li][i]-XT_L[id_lj][i]);
    		    double d_TTI=2.0*(1-alpha-beta)*(XT_T[id_t][i]-XT_L[id_li][i]);
    		    double d_TTJ=-2.0*(1-alpha-beta)*(XT_T[id_t][i]-XT_L[id_lj][i]);
    		    XP_U[id_u][i]=calc_new_theta(XP_U[id_u][i],k,d_PU);
    		    XP_L[id_li][i]=calc_new_theta(XP_L[id_li][i],k,d_PLI);
    		    XP_L[id_lj][i]=calc_new_theta(XP_L[id_lj][i],k,d_PLJ);
    		    XS_L[id_lc][i]=calc_new_theta(XS_L[id_lc][i],k,d_SLC);
    		    XS_L[id_li][i]=calc_new_theta(XS_L[id_li][i],k,d_SLI);
    		    XS_L[id_lj][i]=calc_new_theta(XS_L[id_lj][i],k,d_SLJ);
    		    XT_T[id_t][i]=calc_new_theta(XT_T[id_t][i],k,d_TT);
    		    XT_L[id_li][i]=calc_new_theta(XT_L[id_li][i],k,d_TTI);
    		    XT_L[id_lj][i]=calc_new_theta(XT_L[id_lj][i],k,d_TTJ);
    		}
    	    	
    	}else{
    		double k=1-sigmod(calc_z(id_u,id_lc,id_li,id_lj,id_t,P_WEIGHT,0));
    		for(int i=0;i<NUM_DIM;i++){
    			double d_PU=2.0*P_WEIGHT*(XP_L[id_li][i]-XP_L[id_lj][i]);
    			double d_PLI=2.0*P_WEIGHT*(XP_U[id_u][i]-XP_L[id_li][i]);
    			double d_PLJ=-2.0*P_WEIGHT*(XP_U[id_u][i]-XP_L[id_lj][i]);
    			//double d_TT=2.0*(1-P_WEIGHT)*(XT_L[id_li][i]-XT_L[id_lj][i]);
    			//double d_TTI=2.0*(1-P_WEIGHT)*(XT_T[id_t][i]-XT_L[id_li][i]);
    			//double d_TTJ=-2.0*(1-P_WEIGHT)*(XT_T[id_t][i]-XT_L[id_lj][i]);
    		    XP_U[id_u][i]=calc_new_theta(XP_U[id_u][i],k,d_PU);
    		    XP_L[id_li][i]=calc_new_theta(XP_L[id_li][i],k,d_PLI);
    		    XP_L[id_lj][i]=calc_new_theta(XP_L[id_lj][i],k,d_PLJ);
    		    //XT_T[id_t][i]=calc_new_theta(XT_T[id_t][i],k,d_TT);
    		    //XT_L[id_li][i]=calc_new_theta(XT_L[id_li][i],k,d_TTI);
    		    //XT_L[id_lj][i]=calc_new_theta(XT_L[id_lj][i],k,d_TTJ);
    		}
    	}	
    }
    double cost_func(){
    	double res=0;
    	for(int i=0;i<total_user;i++){
  		  temp_visited[i]=visited[i];
  	  }
    	for(int ui=0;ui<total_user;ui++){
    		for(int i=0;i<user_checkins[ui].size()-1;i++){
    			     int lc=user_checkins[ui].get(i);
    			     int li=user_checkins[ui].get(i+1);
    			     int id_lc=checkins.get(lc).point_id;
    			     int id_li=checkins.get(li).point_id;
    			     int id_t=checkins.get(li).timestamp;
    			     //double flag=(time_diff(lc,li)>THRESHOLD)?1.0:P_WEIGHT;
    			     double flag1=0,flag2=0;
    			     if(time_diff(lc,li)>THRESHOLD){
                          flag1=P_WEIGHT;
                          flag2=0;
    			     }else{
       			    	  flag1=alpha;
    			    	  flag2=beta;
    			     }
    			     for(int j=0;j<total_point;j++){
    			    	 if(!visited[ui].containsKey(j)||visited[ui].get(j)==0){
    			    	     for(int t=0;t<TIME_PERIODS;t++)
    			               res+=Math.log(sigmod(calc_z(ui,id_lc,id_li,j,t,flag1,flag2)));
    			    	 }
    			     }
   				      int a=visited[ui].get(checkins.get(lc).point_id);
   				      visited[ui].put(ui, a-1); 
    			     }
              	}
		return res;
    }
    void export_result(String filename){
    	  try
		  {
		   //创建一个printWriter类的实例，其构造函函数是一个File对象
		   PrintWriter fout = new PrintWriter(filename);
		   
		   //调用writer()方法写入数据
		   fout.write(total_user+" "+total_point+" "+NUM_DIM+"\n");
		   for(int i=0;i<total_user;i++){
			   fout.write(user_id2raw_id[i]+" ");
		   }
		   fout.write("\n");
		   for(int i=0;i<total_point;i++){
			   fout.write(point_id2raw_id[i]+" ");
		   }
		   fout.write("\n");
		   for(int i=0;i<total_point;i++){
			   for(int j=0;j<NUM_DIM;j++){
				   fout.write(XS_L[i][j]+" ");
			   }
			   fout.write("\n");
		   }
		   for(int i=0;i<total_point;i++){
			   for(int j=0;j<NUM_DIM;j++){
				   fout.write(XP_L[i][j]+" ");
			   }
			   fout.write("\n");
		   }
		   for(int i=0;i<total_user;i++){
			   for(int j=0;j<NUM_DIM;j++){
				fout.write(XP_U[i][j]+" ");
			   }
			   fout.write("\n");
		   }
		   for(int i=0;i<TIME_PERIODS;i++){
			   for(int j=0;j<NUM_DIM;j++){
			    fout.write(XT_T[i][j]+" ");
			   }
			   fout.write("\n");
		   }
		   for(int i=0;i<total_point;i++){
			   for(int j=0;j<NUM_DIM;j++){
				   fout.write(XT_L[i][j]+" ");
			   }
			   fout.write("\n");
		   }
		   
		   fout.close();
		  }
		  catch(FileNotFoundException e){
		   System.out.println("找不到文件!错误信息为："+e.getMessage());
		  }
    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	  int time_check=0;
      System.out.println("Loading data...");
      TPrme_social prme=new TPrme_social();
      prme.load_data("d://CASE_GOWALLA.txt");
      prme.load_social_data("d://social_gowalla.txt");
      System.out.println("storage social data");
      prme.store_similarity();
      System.out.println("output the number of total_nz_similarity"+total_nz_similarity);
	  System.out.println("Initializing theta...");	
	  prme. init_theta();
	  for(int i=0;i<total_user;i++){
		  temp_visited[i]=visited[i];
	  }
	  for(int iter=0;iter<NUM_ITER;iter++){
		  System.out.println("Loop["+iter+"]: running...");
		  for(int ui=0;ui<total_user;ui++){
			  for(int i=0;i<user_checkins[ui].size()-1;i++){
				  int lc=user_checkins[ui].get(i);
				  int li=user_checkins[ui].get(i+1);
				  int id_lj=prme.random_choice_unvisited(ui);
				  int a=visited[ui].get(checkins.get(lc).point_id);
				  visited[ui].put(ui, a-1);  
				  prme.update_theta(ui,lc,li,id_lj);
			  }
			
		  }
		  System.out.print("..");
		 if(iter%100==0){
		    // System.out.println("\nCOST:"+prme.cost_func());
		 }
			 
		
		 //if(iter%100==0)System.out.print("\n"+iter+"Cost:"+prme.cost_func());
		 for(int i=0;i<total_user;i++){
			 visited[i]=temp_visited[i];
		 }
	  }
	 //int id= raw_id2user_id.get(547);
	 //System.out.println("bianhao"+id);
	 // System.out.println("Exporting result to file...\n");
	     prme.export_result("d://result_gts_gowalla.txt");
		 Date date=new Date();
	       DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	       String time=format.format(date); 
	       System.out.println(time);
	}

}
             