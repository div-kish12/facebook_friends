import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.util.StringUtils;


public class MutualFriends  {

	public static class Map extends Mapper<LongWritable, Text, Text, Text> {

		private Text user_friends = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			
			String input = value.toString();
			
			String[] input_tokens = input.split("\t");
			
			String user = input_tokens[0];
			Long user_long = Long.parseLong(user);
			
			if (input_tokens.length == 2) {
				
				String friend_list = input_tokens[1];
				
				String[] friend_array = input_tokens[1].split(",");
				
				for(String friend:friend_array){
				
					user_friends.set(friend_list);					
					
					ArrayList<Integer> list1 = new ArrayList<Integer>();
					
					if (user_long.compareTo(Long.parseLong(friend)) < 0) {

					
						list1.clear();
						list1.add(Integer.parseInt(user_long.toString()));
						list1.add(Integer.parseInt(friend));
						
						context.write(new Text(StringUtils.join(",", list1)), user_friends);
					

					} else {
						
						list1.clear();
						
						list1.add(Integer.parseInt(friend));
						list1.add(Integer.parseInt(user_long.toString()));
						
						
						context.write(new Text(StringUtils.join(",", list1)), user_friends);
						
						
					}
				
			}

			}
		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			String[] friend_pool = new String[2];
			int index = 0;
			for (Text v : values) {
				friend_pool[index++] = v.toString();
			}

			if (null != friend_pool[0]) {
				friend_pool[0] = friend_pool[0].replaceAll("[^0-9,]", "");

			}
			if (null != friend_pool[1]) {
				friend_pool[1] = friend_pool[1].replaceAll("[^0-9,]", "");
			}

			String[] l1 = friend_pool[0].split(",");
			String[] l2 = friend_pool[1].split(",");
			
			
			ArrayList<Integer> l3=new ArrayList<Integer>();
			ArrayList<Integer> l4 = new ArrayList<Integer>();
			
			if(null != friend_pool[0]){
				for (String str:l1){
					l3.add(Integer.parseInt(str));
				}
			}
			
			if(null != friend_pool[1]){
				for(String str:l2){
					if(l3.contains(Integer.parseInt(str))){
						l4.add(Integer.parseInt(str));
					}
				}
			}
		
			context.write(new Text(key.toString()), new Text(StringUtils.join(",", l4)));

		}
	}

	public static void main(String args[]) throws Exception {
		Configuration conf = new Configuration();
		
		Job job = Job.getInstance(conf, "MutualFriends");
		
		job.setJarByClass(MutualFriends.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setOutputKeyClass(Text.class);

		job.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));

		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

	
}