import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.DateFormat;

import java.util.concurrent.*;
import java.util.jar.Pack200.Packer;
class CheckIn{
	int user_id;
	double latitude,longitude;
    long time;
	int point_id;
	void debg(){
		System.out.print(point_id+user_id+latitude+longitude+"时间是"+time);

	}
}
class Contrast{
	  int raw_id;
	  int numerator;
	  int p_denominator;
	  int r_denominator;
}
class a{
	int raw_id;
	double recall;
	double precision;
}
class pair{
	 int fist;
	 double second;
	 public pair(){}
	 public pair(int fist, double second ){
	       this.fist=fist;
	       this.second=second ;
	   }
	   
	   public int getfist() {
	       return fist;
	   }

	   public  double  getsecond() {
	       return second;
	   }
}


class MyCompare implements Comparator //实现Comparator，定义自己的比较方法
{
   public int compare(Object o1, Object o2) {
pair e1=(pair)o1;
pair e2=(pair )o2;

if(e1.second >e2.second)//这样比较是降序,如果把-1改成1就是升序.
{
   return 1;
}
else{
	 return -1;
	}
}
}
public class Prme_test {

	/**
	 * @param args
	 */
    static int NUM_TOP_K=10;
	static int MAX_USER=3300;
	static int MAX_POINT=50000;
	static double P_WEIGHT=0.2;
	static int NUM_DIM=50;
	static int NUM_ITER=1000; 
	static double LEARNING_RATE=0.005;
    static double LAMBDA=0.01;
    static int THRESHOLD=6*3600*1000;
    static int MAX_NUM_DIM=70;
	static Vector<CheckIn> checkins=new Vector();
	static  Map<Integer,Integer >  raw_id2user_id=new HashMap();
	static  Map<Integer,Integer >  raw_id2point_id=new HashMap();
	static  Map<Integer,Integer >  new_point_id=new HashMap();
	static  int user_id2raw_id[]=new int[MAX_USER];
	static  int point_id2raw_id[]=new int[MAX_POINT];
	//static  List<Integer> testint_checkins[]=new List[MAX_USER];
	static  Vector<Integer> testing_checkins[]=new Vector[MAX_USER];
	static  Vector<Integer> visited[]=new Vector[MAX_USER];
	static  Vector<Integer> test_visited[]=new Vector[MAX_USER];
	//static Map<Integer,Integer> visited[]=new HashMap[MAX_USER];
	//static Map<Integer,Integer> temp_visited[]=new HashMap[MAX_USER];
	static int cache_topk[][]=new int[MAX_POINT][NUM_TOP_K];
	static double  XS_L[][]=new double [MAX_POINT ][NUM_DIM];
	static double  XP_L[][]=new double [MAX_POINT ][NUM_DIM];
	static double  XP_U[][]=new double [MAX_USER  ][NUM_DIM];
     static int total_user=0;
	 static int total_point=0;
 

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
     public static int  getSecond(Date date){
    	 Calendar calendar =Calendar.getInstance();
    	 calendar.setTime(date);
    	 int intSecond=calendar.get(Calendar.SECOND);
    	 return  intSecond;
     }
     long date2time (Date date){
    	 int year=getYear(date);
    	 int month=getMonthofDate(date);
    	 int day=getDayofMonth(date);
    	 int hour=getHourofDate(date);
    	 int minute=getMinute(date);
    	 int second=getSecond(date);
    	 //long time=(year-1990)*365*24*3600+month*30*24*3600+day*24*3600+hour*3600+minute*60+second;
    	 long time=date.getTime();
    	 return time;
     }
     double dis(double A[],double B[]){
    	 double res=0;
    	 int dim=NUM_DIM;
    	 for(int i=0;i<dim;i++){
    		 res+=(A[i]-B[i])*(A[i]-B[i]);
    	 }
    	 return res;
     }
     double dis_u21[];
     double calc_dp(int u,int l){
    	 if(dis_u21[l]>0) return dis_u21[l];
    	 return dis_u21[l]=dis(XP_L[l],XP_U[u]);
     }
     double calc_ds(int la,int lb){
    	 return dis(XS_L[la],XS_L[lb]);
     }
     double calc_d(int u,int lc,int li,double alpha){
    	 return alpha*calc_dp(u,li)+(1-alpha)*calc_ds(lc,li);
     }
     static int effective_user=0;
     static int new_total_point_id=32151;
     static Vector<String[]> small=new Vector();
     static Map<Integer,Integer> count_user=new HashMap();
     void load_test_data(String file_name){
    	
    	 for(int i=0;i<MAX_USER;i++){
    	     testing_checkins[i]=new Vector();
    	 }
    	 for(int i=0;i<MAX_USER;i++){
    		 test_visited[i]=new Vector();
    	 }
    	 File file=new File(file_name);
    	 BufferedReader br;
         String temp=null;
         try {
        	       br = new BufferedReader(new FileReader(file));
			       temp=br.readLine();
			       while(temp!=null){
			       int id1,id2;
			       CheckIn checkin=new CheckIn();
				   String []catalog=temp.split(" ");
				   id1=Integer.parseInt(catalog[0]);
				   id2=Integer.parseInt(catalog[4]);
				   if(id1==107164)
					   small.add(catalog);
				   
				   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
				    /*  if(!count_user.containsKey(id1)){
						    
						    count_user.put(id1, effective_user++);
				             }*/
                    checkin.time=date2time(date);
                   /* if(!raw_id2user_id.containsKey(id1)){
                    	if(!count_user.containsKey(id1)){
                    		count_user.put(id1, effective_user++);
                    	}
                    }*/
                 /*   if(!raw_id2user_id.containsKey(id1) || !raw_id2point_id.containsKey(id2)) {
                    	 temp=br.readLine();
                    	 
                        continue;
                    }
                 
                    /*if(!count_user.containsKey(id1)){
					    
					    count_user.put(id1, effective_user++);
			             }*/
 				    /*  if(!raw_id2user_id.containsKey(id1)){
					    user_id2raw_id[total_user]=id1;
					    raw_id2user_id.put(id1, total_user++);
			             }*/
                    if(!raw_id2user_id.containsKey(id1) ) {
                   	 temp=br.readLine();
                   	 
                       continue;
                    }
				   if(!raw_id2point_id.containsKey(id2))
				        {
					       point_id2raw_id[total_point]=id2;
					       if(!raw_id2point_id.containsKey(id2))
					       new_point_id.put(id2, new_total_point_id++);
				        }
                    	
                    checkin.user_id = raw_id2user_id.get(id1);
                    if(!raw_id2point_id.containsKey(id2)){
                    checkin.point_id = new_point_id.get(id2);
                    }else{
                    	 checkin.point_id = raw_id2point_id.get(id2);
                    }
                
				    testing_checkins[checkin.user_id].add(checkins.size());
				    checkins.add(checkin);
				   
				    temp=br.readLine();
			       }
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			   e.printStackTrace();
		   }
    	 
     }
     void load_result(String file_name){
    	 File file=new File(file_name);
    	 BufferedReader br;
         String temp=null;
         try {
        	       br = new BufferedReader(new FileReader(file));
			       temp=br.readLine();
			       
			       
			       for(int i=0;i<total_point;i++){
			           String []DIM1=temp.split(" ");
                       for(int j=0;j<NUM_DIM;j++){
                    	   XS_L[i][j]=Double.parseDouble(DIM1[j]);
                       }
			           temp=br.readLine();
			       }
			       
			       
			       for(int i=0;i<total_point;i++){
			    	   String []DIM2=temp.split(" ");
			    	   for(int j=0;j<NUM_DIM;j++){
			    	       XP_L[i][j]=Double.parseDouble(DIM2[j]);
			    	   }
			    	   temp=br.readLine();
			       }
			       for(int i=0;i<total_user;i++){
			    	   String []DIM3=temp.split(" ");
			    	   for(int j=0;j<NUM_DIM;j++){
			    	       XP_U[i][j]=Double.parseDouble(DIM3[j]);
			    	   }
			    	   temp=br.readLine();
			       }
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			   e.printStackTrace();
		   }
     }
     void load_trained_result(String file_name){
    	 File file=new File(file_name);
    	 for(int i=0;i<MAX_USER;i++){
    		 visited[i]=new Vector();
    	 }
    	 BufferedReader br;
         String temp=null;
         try {
        	       br = new BufferedReader(new FileReader(file));
			       temp=br.readLine();
			       String []user_point_dim=temp.split(" ");
			       total_user=Integer.parseInt(user_point_dim[0]);
			       total_point=Integer.parseInt(user_point_dim[1]);
			       NUM_DIM=Integer.parseInt(user_point_dim[2]);
			       temp=br.readLine();
			       String []user2raw=temp.split(" ");
			       for(int i=0;i<total_user;i++){
			    	   user_id2raw_id[i]=Integer.parseInt(user2raw[i]);
			    	   raw_id2user_id.put(user_id2raw_id[i],i);
			       }
			       temp=br.readLine();
			       String []point2raw=temp.split(" ");
			       for(int i=0;i<total_point;i++){
			    	   point_id2raw_id[i]=Integer.parseInt(point2raw[i]);
			    	   raw_id2point_id.put(point_id2raw_id[i], i);
			       }
			       temp=br.readLine();
			       
			       
			       for(int i=0;i<total_point;i++){
			           String []DIM1=temp.split(" ");
                       for(int j=0;j<NUM_DIM;j++){
                    	   XS_L[i][j]=Double.parseDouble(DIM1[j]);
                       }
			           temp=br.readLine();
			       }
			       
			       
			       for(int i=0;i<total_point;i++){
			    	   String []DIM2=temp.split(" ");
			    	   for(int j=0;j<NUM_DIM;j++){
			    	       XP_L[i][j]=Double.parseDouble(DIM2[j]);
			    	   }
			    	   temp=br.readLine();
			       }
			       for(int i=0;i<total_user;i++){
			    	   String []DIM3=temp.split(" ");
			    	   for(int j=0;j<NUM_DIM;j++){
			    	       XP_U[i][j]=Double.parseDouble(DIM3[j]);
			    	   }
			    	   temp=br.readLine();
			       }
			      /* for(int i=0;i<total_user;i++){
			    	   String []visited_location=temp.split(" ");
			    	   for(int j=0;j<visited_location.length;j++){
			    		   visited[i].add(Integer.parseInt(visited_location[j]));
			    	   }
			    	   temp=br.readLine();
			       }*/
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			   e.printStackTrace();
		   }
    	 
     }
 
     static  double r_sum,p_sum;
     static  double r_sum1=0,p_sum1=0;
     static  int total_tested=0;
   
     static  int point_hits[];
     static  int r_sum_fenzi;
     static  int r_sum_fenmu;
     static  int  p_sum_fenzi;
     static  int p_sum_fenmu;
     static  double precision_all;
     static  double precision_fenmu;
     static  double recall_all;
     static  double recall_fenmu;
     void do_test(int id,int all){
    	 int k=NUM_TOP_K;
    	 CheckIn c=checkins.get(id);
    	 long  cur_time=c.time;
    	 int lc=c.point_id;
    	 int u=c.user_id;
    	 
    	 
    		  //Queue<pair> dist = new PriorityQueue<pair>();
    		  PriorityQueue<pair> dist=new PriorityQueue<pair>(10,idComparator );
    		 for(int i=0;i<total_point;i++){
    			 double d=calc_d(u,lc,i,P_WEIGHT);
    			 if(d==1.8145121543150875)
    				 System.out.println("error");
    			 pair make_pair=new pair();
    			 if(dist.size()<k){
    				// if(point_id2raw_id[i]==9241)
						// System.out.println(i);
    				 if(!test_visited[u].contains(i)){
    				 make_pair=new pair(i,d);
    				 //make_pair.second=d;
    				 dist.add(make_pair);}
    				 }else if(d<dist.peek().second&&!test_visited[u].contains(i)){
    					 dist.poll();
    				
    					 //if(point_id2raw_id[i]==9241)
    						// System.out.println(i);
    					 make_pair=new pair(i,d);
    					 dist.add(make_pair);
    				 }
    		 }
    		 
    	        //System.out.print("u="+user_id2raw_id[u]+" "+"lc="+point_id2raw_id[lc]+"topk="+" ");
    		 
    		 for(int i=0;i<k;i++){
    			 int pid=dist.peek().fist;
    			 if(user_id2raw_id[u]==66){
    		     //System.out.print(point_id2raw_id[pid]+" ");
    		     //System.out.print("raw_user="+u+"　");
    			 }
    			 cache_topk[lc][i]=pid;
    			 dist.poll();
    			 
    		 }
    		// if(user_id2raw_id[u]==66)
    		 //System.out.print("\n");
    		 Scanner ss=new Scanner(System.in);
    		// ss.nextInt();
    	 
    	 

    	    int in_topk = 0;
    	    int correct = 0;
    	    for(int i = 0; i < k; i++) {
    	        int pid = cache_topk[lc][i];
    	        if(point_hits[pid] > 0) in_topk++;
    	        if(!test_visited[u].contains(pid))
    	        correct += point_hits[pid];
    	    }
    	    //System.out.println(user_id2raw_id[u]+"   "+correct);
            r_sum_fenzi+=correct;
            r_sum_fenmu+=all;
            p_sum_fenzi+=correct;
            precision_all+=correct;
            recall_all+=correct;
            recall_fenmu+=all;
            precision_fenmu+=k;
            p_sum_fenmu+=k;
    	    total_tested ++;
    	    r_sum1 += 1.0 * correct / all;
    	    p_sum1 += 1.0 * correct / k;
     }
     static  int flag=0;
     static  int num=0;
     Vector v=new Vector();
     Vector<Double> R_SUM=new Vector();
     Vector<Double> P_SUM=new Vector();
     static int user_total=0;
     static int al=0;
     static  Vector<Contrast> my_result=new Vector();
     void test(){
    	 r_sum=0;p_sum=0;
    	 total_tested=0;
    	 //int all=checkins.size();
    	 int now =1;
    	 
          for(int u=0;u<total_user;u++){
        	  r_sum_fenzi=0;
        	  r_sum_fenmu=0;
        	  p_sum_fenzi=0;
        	  p_sum_fenmu=0;
        	  point_hits=new int[MAX_POINT];
        	  for(int i=0;i<point_hits.length;i++){
        		  point_hits[i]=0;
        	  }
        	  dis_u21=new double[MAX_POINT];
        	  for(int p=0;p<total_point;p++) cache_topk[p][0]=-1;
         	 if(u%100==0) System.out.println("Testing user:"+u);
         	 for(int l=0,r=0;l<testing_checkins[u].size();l++){
         		 int id=testing_checkins[u].get(l);
         		 long cur_time=checkins.get(id).time;
         		 int cur_pid=checkins.get(id).point_id;
         		 int cur=point_id2raw_id[cur_pid];
         		 if(user_id2raw_id[u]==98)
         		  cur=point_id2raw_id[cur_pid];
         		    
         		 while(r<testing_checkins[u].size()&&(checkins.get(testing_checkins[u].get(r)).time)<=cur_time+THRESHOLD){
         			
         			 int pid=checkins.get(testing_checkins[u].get(r)).point_id;
         			 int pid2=point_id2raw_id[pid];
         			// if(r>0)
         			 v.add(pid2);
         			// if(!visited[u].contains(pid))
         			 point_hits[pid]++;
         			 r++;
         		 }
         		
         		 point_hits[cur_pid]--;
         		 int all=r-l-1;
         		if(!raw_id2point_id.containsKey(cur))
         			all=0;
         		if(user_id2raw_id[u]==66)
         		{
         			//System.out.println(all);
         			al+=all;
         		}
         		 if(!test_visited[u].contains(cur_pid))
              		test_visited[u].add(cur_pid);
         		 if(all>0)  
         			 do_test(id,all);
         		
         		 v.clear();
         	 }
         	 
         	 if(r_sum_fenmu>0){
         	 r_sum+=(r_sum_fenzi/r_sum_fenmu);
         	 double ter=1.0*r_sum_fenzi/r_sum_fenmu;
         	 int raw_id=user_id2raw_id[u];
        	// if(user_id2raw_id[u]==107164)
         	 {
        		// System.out.println(user_id2raw_id[u]+"   "+r_sum_fenzi+"    "+r_sum_fenmu+"  "+p_sum_fenmu);
         	 }
         	 Contrast result=new Contrast();
         	 result.raw_id=user_id2raw_id[u];
         	 result.numerator=(int) r_sum_fenzi;
         	 result.p_denominator=(int)p_sum_fenmu;
         	 result.r_denominator=(int) r_sum_fenmu;
         	 my_result.add(result);
            // System.out.println(user_id2raw_id[u]+"   "+r_sum_fenzi+"    "+r_sum_fenmu+"  "+p_sum_fenmu);
         	//System.out.println(user_id2raw_id[u]+"   "+r_sum_fenzi);
     		
         	 
         	 R_SUM.add(ter);
        
         	if(!count_user.containsKey(u)){
			    
			    count_user.put(u, effective_user++);
			    
	             }
         	 }
         	 if(p_sum_fenmu>0){
         	   p_sum+=(p_sum_fenzi/p_sum_fenmu); 
         	  double tep=1.0*p_sum_fenzi/p_sum_fenmu;
          	   P_SUM.add(tep);
         	 }
          }
          double temp_r_sum=0;
          double temp_p_sum=0;
         for(int i=0;i<R_SUM.size();i++){
        	 temp_r_sum+=R_SUM.get(i);
        	 
         }
         for(int i=0;i<P_SUM.size();i++){
        	 temp_p_sum+=P_SUM.get(i);
         }
         double recall=temp_r_sum/R_SUM.size();
         double precision=temp_p_sum/P_SUM.size();
    	 double precision1=p_sum1/total_tested;
    	 double recall1=r_sum1/total_tested;
    	 
         //double precision=p_sum/effective_user;
    	 //double recall=r_sum/effective_user;
    	 System.out.println("有效记录数是"+total_tested);
    	 System.out.println("有效用户数是"+R_SUM.size());
    	 System.out.println("Total precision="+precision1);
    	 System.out.println("Total recall="+recall1);
    	 System.out.println("-------------------------------------------------------------");
    	 //System.out.println("除以用户数Total precision="+precision);
    	// System.out.println("除以用户数Total recall="+recall);
    	 System.out.println("总的精度的分子="+precision_all);
    	 System.out.println("总的精度的分母="+precision_fenmu);
    	 System.out.println("总的recall的分子="+recall_all);
    	 System.out.println("总的recall的分母="+recall_fenmu);
     }
    static Vector<Contrast> compare=new Vector();
     void read_campare(String file_name){
       	 File file=new File(file_name);
    	 BufferedReader br;
         String temp=null;
         try {
        	       br = new BufferedReader(new FileReader(file));
			       temp=br.readLine();
			       while(temp!=null){
			       int id1,id2,id3,id4;
			       Contrast contrast=new Contrast();
				   String []catalog=temp.split(": ");
				   id1=Integer.parseInt(catalog[0]);
				   id2=Integer.parseInt(catalog[1]);
				   id3=Integer.parseInt(catalog[2]);
				   id4=Integer.parseInt(catalog[3]);
				   contrast.raw_id=id1;
				   contrast.numerator=id2;
				   contrast.p_denominator=id3;
				   contrast.r_denominator=id4;
                   compare.add(contrast);
				   temp=br.readLine();
			       }
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			   e.printStackTrace();
		   }
    	 
     }
     void export_result(String filename){
   	  try
		  {
		   //创建一个printWriter类的实例，其构造函函数是一个File对象
		   PrintWriter fout = new PrintWriter(filename);
		   
		   for(int id:testing_checkins[1]){
			
			   fout.write(checkins.get(id).user_id+" "+checkins.get(id).time+" "+checkins.get(id).latitude+" "+checkins.get(id).longitude+" "+checkins.get(id).point_id+"\n");
		   }
		  /* for(int i=0;i<total_user;i++){
			   visit=visited[i].keySet();

			 
	            for(int value:visit){
	               fout.write(value+" ");	
	            }
	            fout.write("\n");
			   
		   }
		   */
		   fout.close();
		  }
		  catch(FileNotFoundException e){
		   System.out.println("找不到文件!错误信息为："+e.getMessage());
		  }
   }
   

	public static void main(String[] args) {
		// TODO Auto-generated method stub
      System.out.println("Loading training result...");
      Prme_test prme=new Prme_test();
      prme.load_trained_result("d://prme_s_gowalla.txt");
      /*int id2=raw_id2user_id.get(2);
      int id3=raw_id2point_id.get(9225);
      System.out.println(id2);
      System.out.println("user");
      for(int d=0;d<50;d++){
    	 System.out.print( XP_U[id2][d]+" ");
      }
      System.out.print("\n");
      System.out.println("S location");
      for(int d=0;d<50;d++){
     	 System.out.print( XS_L[id3][d]+" ");
       }
       System.out.print("\n");
       System.out.println("Q location");
       for(int d=0;d<50;d++){
       	 System.out.print( XP_L[id3][d]+" ");
         }
         System.out.print("\n");*/
	  System.out.println("Loading data...");	
	  prme.load_test_data("d://CASE_GOWALLA_TEST.txt");
	  // prme.read_campare("d://contrast.txt");
	//  prme.load_test_data("d://test_set_big.txt");
	  try
	  {
	   //创建一个printWriter类的实例，其构造函函数是一个File对象
	   PrintWriter fout = new PrintWriter("d:\\temp_java6.txt");
	   
	   
		   for(int i=0;i<small.size();i++){
		   String[] catalog=small.get(i);
		   fout.write(catalog[0]+" "+catalog[1]+" "+catalog[2]+" "+catalog[3]+" "+catalog[4]+"\n");
		   }
	  /* for(int i=0;i<total_user;i++){
		   visit=visited[i].keySet();

		 
            for(int value:visit){
               fout.write(value+" ");	
            }
            fout.write("\n");
		   
	   }
	   */
	  fout.close();
	  }
	  catch(FileNotFoundException e){
	   System.out.println("找不到文件!错误信息为："+e.getMessage());
	  }
	  System.out.println("测试集中"+effective_user);
	   System.out.println("打出标记user_num"+num);
	  //prme.export_result("d://test_small.txt");
      prme.test();
   /*   for(int i=0;i<my_result.size();i++){
    	  int temp1,temp2,temp3,temp4;
    	  
    	  for(int j=0;j<compare.size();j++){
    		  if(my_result.get(i).raw_id==compare.get(j).raw_id){
    			    if(my_result.get(i).numerator!=compare.get(j).numerator||my_result.get(i).p_denominator!=compare.get(j).p_denominator||my_result.get(i).r_denominator!=compare.get(j).r_denominator)
    			         System.out.println("不同的用户id是"+my_result.get(i).raw_id+"  "+my_result.get(i).numerator+"  "+my_result.get(i).p_denominator+"  "+my_result.get(i).r_denominator);
    		  }
    	  }
      }*/
		 Date date=new Date();
	       DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	       String time=format.format(date); 
	       System.out.println(time);
	 
	}
	public static Comparator<pair> idComparator = new Comparator<pair>(){
		 
        public int compare(pair c1, pair c2) {
            //return (int) (c1.getsecond() - c2.getsecond());
        	if(c1.second >c2.second)//这样比较是降序,如果把-1改成1就是升序.
        	{
        	   return -1;
        	}
        	else{
        		 return 1;
        		}
        }
    };

}
             