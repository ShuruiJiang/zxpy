import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    static ArrayList<Integer> size_list = new ArrayList<>();  //节点大小
    static ArrayList<Integer> node_list = new ArrayList<>();  //节点
    static Map<Integer, Integer> node_size = new HashMap<>();
    static List<List<Integer>> contain_1B = new ArrayList<>(64);
    static List<List<Integer>> contain_2B = new ArrayList<>(88);
    static List<List<Integer>> contain_4B = new ArrayList<>(68);
    static Map<Integer, Integer> colorAssignment = new HashMap<>(); //节点是什么颜色
    static Map<Integer, Set<Integer>> colorNodes = new HashMap<>(); //某个颜色有多少节点
    static int num4 = 0;

    public static void main(String[] args) {
        File dataDirectory = new File("./data/");
        if (!dataDirectory.exists()) {
            System.out.println("The data directory does not exist.");
            return;
        }

        File[] subdirectories = dataDirectory.listFiles((dir, name) -> new File(dir, name).isDirectory());
        if (subdirectories == null) {
            System.out.println("An error occurred while listing directories.");
            return;
        }

        for (File subdirectory : subdirectories) {
            Path subdirPath = subdirectory.toPath();
            Path csvPath = subdirPath.resolve("input.csv");

            // 检查 input.csv 文件是否存在
            if (Files.exists(csvPath)) {
                System.out.println("Found CSV file at: " + csvPath);
                // 在这里可以执行读写操作，例如：
                // Files.readAllLines(csvPath) 或者 Files.write(...)
            } else {
                System.out.println("No input.csv found in: " + subdirPath);
            }
        }
        for (File subdirectory : subdirectories) {
            Path subdirPath = subdirectory.toPath();
            System.out.println("number" + subdirPath);
            Path csvPath = subdirPath.resolve("input.csv");
            if (Files.exists(csvPath)) {
                size_list.clear();
                node_list.clear();
                node_size.clear();
                contain_1B.clear();
                contain_2B.clear();
                contain_4B.clear();
                colorAssignment.clear();
                colorNodes.clear();
                main1(subdirPath.toString());
                size_list.clear();
                node_list.clear();
                node_size.clear();
//                contain_1B.clear();
//                contain_2B.clear();
//                contain_4B.clear();
                main2(subdirPath.toString());
            }
        }
    }

    public static void main1(String csvPath) {
        Graph graph = new Graph();
        // 读文件
        Path pathfile = Paths.get(csvPath, "input.csv");
        System.out.println(pathfile.toString());
        readFile(graph, pathfile.toString());

        List<List<Integer>> paths = graph.findPathsFromSourceToSink();
        for (List<Integer> path : paths) {
            System.out.println(path);
        }
        List<List<Integer>> orders = new ArrayList<>();
        try {
            orders = graph.topologicalSort(node_list.size(), size_list);
            if (!orders.isEmpty()) {
                for (List<Integer> order : orders) {
                    System.out.println("Parallel Nodes: " + order);
                }
            } else {
                orders = graph.topologicalSort(node_list.size(), size_list);
                for (List<Integer> order : orders) {
                    System.out.println("Parallel Nodes: " + order);
                }
            }
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
        List<Integer> flattenedOrders = new ArrayList<>();  //拓扑顺序
        Map<Integer, Integer> put_flag2 = new LinkedHashMap<>();
        for (List<Integer> order : orders) {
            flattenedOrders.addAll(order);
            for(Integer integer: order){
                put_flag2.put(integer,1);
            }
        }
        if(put_flag2.size()!=node_list.size()){
            for(int node:node_list){
                if(!put_flag2.containsKey(node)) {
                    flattenedOrders.add(0, node);
                    List<Integer> list1 = new ArrayList<>();
                    list1.add(node);
                    orders.add(0, list1);
                }
            }
        }
        // 判断会不会超过内存
        num4=0;
        for(List<Integer> order:orders){
            int max_field = 0;
            for(Integer integer:order){
                if(size_list.get(integer)>max_field){
                    max_field = size_list.get(integer);
                    if(max_field>=24){
                        num4++;
                        break;
                    }
                }
            }
        }

        // 还没存为0，存了为1
        Map<Integer, Integer> node_store = new HashMap<>();
        Map<Integer, List<String>> node_pos = new HashMap<>();
        Map<Integer, List<Integer>> node_no_choose = new HashMap<>();

        for (int i = 0; i < 64; i++) {
            contain_1B.add(new ArrayList<>());
        }
        for (int i = 0; i < 88; i++) {
            contain_2B.add(new ArrayList<>());
        }
        for (int i = 0; i < 68; i++) {
            contain_4B.add(new ArrayList<>());
        }

        for (Integer integer : node_list) {
            node_store.put(integer, 0);
            node_pos.put(integer, new ArrayList<>());
        }
        System.out.println("num4 "+num4);
//        num4 = 100;
        if(num4<68){
            int j=0;
            for (List<Integer> order : orders) {
                node_no_choose.put(j,order);
                j++;
            }
        }else{
            Map<Integer, Integer> sortedEntries1 = new LinkedHashMap<>();
            for (int key : flattenedOrders) {
                if(node_size.get(key)>=24)
                    sortedEntries1.put(key, node_size.get(key));
            }
            for (int key : flattenedOrders) {
                if(node_size.get(key)<24 && node_size.get(key)>=16)
                    sortedEntries1.put(key, node_size.get(key));
            }
            for (int key : flattenedOrders) {
                if(node_size.get(key)<16)
                    sortedEntries1.put(key, node_size.get(key));
            }

            Map<Integer, Set<Integer>> conflictGraph = new HashMap<>();  //冲突图
            conflictGraph = graph.buildConflictGraph(paths);


            // 将Map的条目转换为一个List，并根据值（value）从大到小排序
            List<Map.Entry<Integer, Integer>> sortedEntries = node_size.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList());


//        Map<Integer, Integer> colorAssignment = new HashMap<>(); //节点是什么颜色
//        Map<Integer, List<Integer>> colorNodes = new HashMap<>(); //某个颜色有多少节点
            int numColors = 0;
            for (Map.Entry<Integer, Integer> entry : sortedEntries1.entrySet()){
                Integer key = entry.getKey();
                Integer value = entry.getValue();
                boolean colored = false;
                for (int i = 0; i < numColors; i++) {
                    int flag = 0;
                    for (int conflictingNode : conflictGraph.getOrDefault(key, new HashSet<>())) {
                        if (colorAssignment.getOrDefault(conflictingNode, -1) == i) {
                            flag = 1;  //说明和他冲突的节点以及涂了这个颜色
                            break;
                        }
                    }
                    if (flag==0) {
                        colorAssignment.put(key, i);
                        colorNodes.get(i).add(key);
                        colored = true;
                        break;
                    }
                }
                // 如果没有找到合适的颜色，则分配一个新的颜色
                if (!colored) {
                    colorAssignment.put(key, numColors);
                    Set<Integer> set = new HashSet<>();
                    set.add(key);
                    colorNodes.put(numColors, new HashSet<>(set));
                    numColors++;
                }
            }

            for (Map.Entry<Integer, Integer> entry : colorAssignment.entrySet()){
                Integer key = entry.getKey();
                Integer value = entry.getValue();
                if(!node_no_choose.containsKey(value)){
                    List<Integer> list1 = new ArrayList<>();
                    list1.add(key);
                    node_no_choose.put(value, list1);  // node_no_choose的索引是颜色
                }else {
                    List<Integer> list1 = node_no_choose.get(value);
                    list1.add(key);
                }
            }
        }

        // 存放字段
        for (Map.Entry<Integer, List<Integer>> entry1 : node_no_choose.entrySet()) {
            List<Integer> a1 = entry1.getValue();
            //比较哪个字段长，先放哪个
            int max_field = 0;
            int max_index = 0;
            for (Integer b1 : a1) {
                if (size_list.get(b1) > max_field) {
                    max_field = size_list.get(b1);
                    max_index = b1;
                }
            }
            if (size_list.get(max_index) == 8) {
                for (int i = 0; i < contain_1B.size(); i++) {
                    if (contain_1B.get(i).isEmpty()) {
                        for (Integer b1 : a1) {
                            contain_1B.get(i).add(b1);
                            node_store.put(b1, 1);
                            List<String> list1 = node_pos.get(b1);
                            list1.add(String.valueOf(i));
                        }
                        break;
                    }
                }
                if (node_store.get(max_index) == 0) { //1B没有空间了
                    for (int i = 0; i < contain_2B.size(); i++) {
                        if (contain_2B.get(i).isEmpty() || contain_2B.get(i).size() == 1) {
                            for (Integer b1 : a1) {
                                if (size_list.get(b1) == 8) {
                                    contain_2B.get(i).add(b1);
                                    node_store.put(b1, 1);
                                    List<String> list1 = node_pos.get(b1);
                                    if (contain_2B.get(i).isEmpty()) {
                                        list1.add(String.valueOf(64 + 2 * i));
                                    } else {
                                        list1.add(String.valueOf(64 + 2 * i + 1));
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                if (node_store.get(max_index) == 0) { //1B,2B没有空间了
                    for (int i = 0; i < contain_4B.size(); i++) {
                        if (contain_4B.get(i).isEmpty() || contain_4B.get(i).size() < 4) {
                            for (Integer b1 : a1) {
                                if (size_list.get(b1) == 8) {
                                    contain_4B.get(i).add(b1);
                                    node_store.put(b1, 1);
                                    List<String> list1 = node_pos.get(b1);
                                    if (contain_4B.get(i).isEmpty()) {
                                        list1.add(String.valueOf(240 + 4 * i));
                                    } else if (contain_4B.get(i).size() == 1) {
                                        list1.add(String.valueOf(240 + 4 * i + 1));
                                    } else if (contain_4B.get(i).size() == 2) {
                                        list1.add(String.valueOf(240 + 4 * i + 2));
                                    } else if (contain_4B.get(i).size() == 3) {
                                        list1.add(String.valueOf(240 + 4 * i + 3));
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                if (node_store.get(max_index) == 0) { //1B,2B,4B没有空间了
                    for (Integer b1 : a1) {
                        if (size_list.get(b1) == 8) {
//                            contain_4B.get(i).add(b1);
                            node_store.put(b1, 1);
                            List<String> list1 = node_pos.get(b1);
                            list1.add(String.valueOf(240 + 4 * 67));
                        }
                    }
                }
            }
            if (size_list.get(max_index) == 16) {
                for (int i = 0; i < contain_2B.size(); i++) {
                    if (contain_2B.get(i).isEmpty()) {
                        for (Integer b1 : a1) {
                            if (size_list.get(b1) == 16) {
                                List<Integer> a2 = contain_2B.get(i);
                                a2.add(b1);
                                a2.add(b1);
                                node_store.put(b1, 1);
                                List<String> list1 = node_pos.get(b1);
                                list1.add(String.valueOf(64 + 2 * i));
                                list1.add(String.valueOf(64 + 2 * i + 1));
                            }
                            if (size_list.get(b1) == 8) {
                                contain_2B.get(i).add(b1);
                                node_store.put(b1, 1);
                                List<String> list1 = node_pos.get(b1);
                                list1.add(String.valueOf(64 + 2 * i));
                            }
                        }
                        break;
                    }
                }
                if (node_store.get(max_index) == 0) { //2B没有空间了
                    for (int i = 0; i < contain_4B.size(); i++) {
                        if (contain_4B.get(i).isEmpty() || contain_4B.get(i).size() == 2) {
                            for (Integer b1 : a1) {
                                if (size_list.get(b1) == 16) {
                                    List<Integer> a2 = contain_4B.get(i);
                                    a2.add(b1);
                                    a2.add(b1);
                                    node_store.put(b1, 1);
                                    List<String> list1 = node_pos.get(b1);
                                    if (contain_4B.get(i).isEmpty()) {
                                        list1.add(String.valueOf(240 + 4 * i));
                                        list1.add(String.valueOf(240 + 4 * i + 1));
                                    } else {
                                        list1.add(String.valueOf(240 + 4 * i + 2));
                                        list1.add(String.valueOf(240 + 4 * i + 3));
                                    }
                                }
                                if (size_list.get(b1) == 8) {
                                    contain_4B.get(i).add(b1);
                                    node_store.put(b1, 1);
                                    List<String> list1 = node_pos.get(b1);
                                    if (contain_4B.get(i).isEmpty()) {
                                        list1.add(String.valueOf(240 + 4 * i));
                                    } else {
                                        list1.add(String.valueOf(240 + 4 * i + 2));
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                if (node_store.get(max_index) == 0) { //2B,4B没有空间了
                    for (Integer b1 : a1) {
                        if (size_list.get(b1) == 8) {
//                            contain_4B.get(i).add(b1);
                            node_store.put(b1, 1);
                            List<String> list1 = node_pos.get(b1);
                            list1.add(String.valueOf(240 + 4 * 67));
                        }
                        if (size_list.get(b1) == 16) {
//                            contain_4B.get(i).add(b1);
                            node_store.put(b1, 1);
                            List<String> list1 = node_pos.get(b1);
                            list1.add(String.valueOf(240 + 4 * 67));
                            list1.add(String.valueOf(240 + 4 * 67 + 1));
                        }
                    }
                }
            }
            if (size_list.get(max_index) == 32 || size_list.get(max_index) == 24) {
                for (int i = 0; i < contain_4B.size(); i++) {
                    if (contain_4B.get(i).isEmpty()) {
                        for (Integer b1 : a1) {
                            if (size_list.get(b1) == 32) {
                                List<Integer> a4 = contain_4B.get(i);
                                a4.add(b1);
                                a4.add(b1);
                                a4.add(b1);
                                a4.add(b1);
                                node_store.put(b1, 1);
                                List<String> list1 = node_pos.get(b1);
                                list1.add(String.valueOf(240 + 4 * i));
                                list1.add(String.valueOf(240 + 4 * i + 1));
                                list1.add(String.valueOf(240 + 4 * i + 2));
                                list1.add(String.valueOf(240 + 4 * i + 3));
                            }
                            if (size_list.get(b1) == 24) {
                                List<Integer> a4 = contain_4B.get(i);
                                a4.add(b1);
                                a4.add(b1);
                                a4.add(b1);
                                node_store.put(b1, 1);
                                List<String> list1 = node_pos.get(b1);
                                list1.add(String.valueOf(240 + 4 * i));
                                list1.add(String.valueOf(240 + 4 * i + 1));
                                list1.add(String.valueOf(240 + 4 * i + 2));
                            }
                            if (size_list.get(b1) == 16) {
                                List<Integer> a2 = contain_4B.get(i);
                                a2.add(b1);
                                a2.add(b1);
                                node_store.put(b1, 1);
                                List<String> list1 = node_pos.get(b1);
                                list1.add(String.valueOf(240 + 4 * i));
                                list1.add(String.valueOf(240 + 4 * i + 1));
                            }
                            if (size_list.get(b1) == 8) {
                                contain_4B.get(i).add(b1);
                                node_store.put(b1, 1);
                                List<String> list1 = node_pos.get(b1);
                                list1.add(String.valueOf(240 + 4 * i));
                            }
                        }
                        break;
                    }
                }
                if (node_store.get(max_index) == 0) { //4B没有空间了
                    for (Integer b1 : a1) {
                        if (size_list.get(b1) == 32) {
                            List<Integer> a4 = contain_4B.get(67);
//                            a4.set(0, b1);
//                            a4.set(1, b1);
//                            a4.set(2, b1);
//                            a4.set(3, b1);
                            node_store.put(b1, 1);
                            List<String> list1 = node_pos.get(b1);
                            list1.add(String.valueOf(240 + 4 * 67));
                            list1.add(String.valueOf(240 + 4 * 67 + 1));
                            list1.add(String.valueOf(240 + 4 * 67 + 2));
                            list1.add(String.valueOf(240 + 4 * 67 + 3));
                        }
                        if (size_list.get(b1) == 24) {
                            List<Integer> a4 = contain_4B.get(67);
//                            a4.add(b1);
//                            a4.add(b1);
//                            a4.add(b1);
                            node_store.put(b1, 1);
                            List<String> list1 = node_pos.get(b1);
                            list1.add(String.valueOf(240 + 4 * 67));
                            list1.add(String.valueOf(240 + 4 * 67 + 1));
                            list1.add(String.valueOf(240 + 4 * 67 + 2));
                        }
                        if (size_list.get(b1) == 16) {
                            List<Integer> a2 = contain_4B.get(67);
//                            a2.add(b1);
//                            a2.add(b1);
                            node_store.put(b1, 1);
                            List<String> list1 = node_pos.get(b1);
                            list1.add(String.valueOf(240 + 4 * 67));
                            list1.add(String.valueOf(240 + 4 * 67 + 1));
                        }
                        if (size_list.get(b1) == 8) {
//                            contain_4B.get(67).add(b1);
                            node_store.put(b1, 1);
                            List<String> list1 = node_pos.get(b1);
                            list1.add(String.valueOf(240 + 4 * 67));
                        }
                    }
                }
            }
        }

        for (Map.Entry<Integer, List<String>> entry : node_pos.entrySet()) { //还有剩余
            Integer key = entry.getKey();
            List<String> value = entry.getValue();
            if (value.isEmpty()) {
                if (size_list.get(key) == 8) {
                    for (int i = 0; i < contain_1B.size(); i++) {
                        if (contain_1B.get(i).isEmpty()) {
                            contain_1B.get(i).add(key);
                            node_store.put(key, 1);
                            List<String> list1 = node_pos.get(key);
                            list1.add(String.valueOf(i));
                            break;
                        }
                    }
                }
                if (size_list.get(key) == 16) {
                    for (int i = 0; i < contain_2B.size(); i++) {
                        if (contain_2B.get(i).isEmpty()) {
                            List<Integer> a2 = contain_2B.get(i);
                            a2.add(key);
                            a2.add(key);
                            node_store.put(key, 1);
                            List<String> list1 = node_pos.get(key);
                            list1.add(String.valueOf(64 + 2 * i));
                            list1.add(String.valueOf(64 + 2 * i + 1));
                            break;
                        }
                    }
//                    if(node_store.get(key)==0){
//                        for (int i=0;i<contain_4B.size();i++){
//                            if (contain_4B.get(i).isEmpty()){
//                                List<Integer> a2 = contain_4B.get(i);
//                                a2.add(key);
//                                a2.add(key);
//                                node_store.put(key, 1);
//                                List<String> list1 = node_pos.get(key);
//                                list1.add(String.valueOf(240+4*i));
//                                list1.add(String.valueOf(240+4*i+1));
//                                break;
//                            }
//                        }
//                    }
                }
                if (size_list.get(key) == 32 || size_list.get(key) == 24) {
                    for (int i = 0; i < contain_4B.size(); i++) {
                        if (contain_4B.get(i).isEmpty()) {
                            if (size_list.get(key) == 32) {
                                List<Integer> a4 = contain_4B.get(i);
                                a4.add(key);
                                a4.add(key);
                                a4.add(key);
                                a4.add(key);
                                node_store.put(key, 1);
                                List<String> list1 = node_pos.get(key);
                                list1.add(String.valueOf(240 + 4 * i));
                                list1.add(String.valueOf(240 + 4 * i + 1));
                                list1.add(String.valueOf(240 + 4 * i + 2));
                                list1.add(String.valueOf(240 + 4 * i + 3));
                            }
                            if (size_list.get(key) == 24) {
                                List<Integer> a4 = contain_4B.get(i);
                                a4.add(key);
                                a4.add(key);
                                a4.add(key);
                                node_store.put(key, 1);
                                List<String> list1 = node_pos.get(key);
                                list1.add(String.valueOf(240 + 4 * i));
                                list1.add(String.valueOf(240 + 4 * i + 1));
                                list1.add(String.valueOf(240 + 4 * i + 2));
                            }
                            break;
                        }
                    }
                }
                if (node_store.get(key) == 0) {
                    if (size_list.get(key) == 32) {
                        List<Integer> a4 = contain_4B.get(67);
//                            a4.add(key);
//                            a4.add(key);
//                            a4.add(key);
//                            a4.add(key);
                        node_store.put(key, 1);
                        List<String> list1 = node_pos.get(key);
                        list1.add(String.valueOf(240 + 4 * 67));
                        list1.add(String.valueOf(240 + 4 * 67 + 1));
                        list1.add(String.valueOf(240 + 4 * 67 + 2));
                        list1.add(String.valueOf(240 + 4 * 67 + 3));
                    }
                    if (size_list.get(key) == 24) {
                        List<Integer> a4 = contain_4B.get(67);
//                            a4.add(key);
//                            a4.add(key);
//                            a4.add(key);
                        node_store.put(key, 1);
                        List<String> list1 = node_pos.get(key);
                        list1.add(String.valueOf(240 + 4 * 67));
                        list1.add(String.valueOf(240 + 4 * 67 + 1));
                        list1.add(String.valueOf(240 + 4 * 67 + 2));
                    }
                    if (size_list.get(key) == 16) {
                        List<Integer> a4 = contain_4B.get(67);
//                            a4.add(key);
//                            a4.add(key);
                        node_store.put(key, 1);
                        List<String> list1 = node_pos.get(key);
                        list1.add(String.valueOf(240 + 4 * 67));
                        list1.add(String.valueOf(240 + 4 * 67 + 1));
                    }
                    if (size_list.get(key) == 8) {
                        List<Integer> a4 = contain_4B.get(67);
//                            a4.add(key);
                        node_store.put(key, 1);
                        List<String> list1 = node_pos.get(key);
                        list1.add(String.valueOf(240 + 4 * 67));
                    }
                }
            }
        }


        int result = 0;  // 求一共占了多少内存
        for (List<Integer> list : contain_1B) {
            if (!list.isEmpty()) {
                result += 1;
            }
        }
        for (List<Integer> list : contain_2B) {
            if (!list.isEmpty()) {
                result += 2;
            }
        }
        for (List<Integer> list : contain_4B) {
            if (!list.isEmpty()) {
                result += 4;
            }
        }
        pathfile = Paths.get(csvPath, "output1.csv");
        writeFile(node_pos, pathfile.toString());
        System.out.println("end " + result);

    }

    public static void main2(String csvPath){
        Graph graph = new Graph();
        Map<Integer, List<Integer>> node_pos = new HashMap<>(); //内存位置
        // 读文件
        Path pathfile = Paths.get(csvPath, "input.csv");
        readFile(graph, pathfile.toString());
        pathfile = Paths.get(csvPath, "output1.csv");
        readFile2(node_pos, pathfile.toString());

        List<List<Integer>> paths = graph.findPathsFromSourceToSink();
//        for (List<Integer> path : paths) {
//            System.out.println(path);  //打印路径
//        }

        List<List<Integer>> orders = new ArrayList<>();
        try {
            orders = graph.topologicalSort(node_list.size(), size_list);
            for (List<Integer> order : orders) {
                System.out.println("Parallel Nodes: " + order);
            }
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }

        Map<Integer, Integer> put_flag2 = new LinkedHashMap<>();
        List<Integer> flattenedOrders = new ArrayList<>();  //拓扑顺序
        for (List<Integer> order : orders) {
            flattenedOrders.addAll(order);
            for(Integer integer: order){
                put_flag2.put(integer,1);
            }
        }
        if(put_flag2.size()!=node_list.size()){
            for(int node:node_list){
                if(!put_flag2.containsKey(node)) {
                    List<Integer> list1 = new ArrayList<>();
                    list1.add(node);
                    orders.add(0, list1);
                }
            }
        }

        Map<Integer, Integer> put_flag = new LinkedHashMap<>();
        Map<Integer, List<Integer>> node_no_choose = new LinkedHashMap<>();
        if(num4<68){
            int j=0;
            for (List<Integer> order : orders) {
                node_no_choose.put(j,order);
                j++;
            }
        }else{
            Map<Integer, Set<Integer>> sortedNode = new LinkedHashMap<>();
            Map<Integer, Set<Integer>> sortedColor = new LinkedHashMap<>();
            int i=0;
            for (List<Integer> order : orders){  // 一个平行集合中有几个颜色
                Set<Integer> set1 = new HashSet<>();
                for(Integer integer:order){
                    set1.add(colorAssignment.get(integer));
                }
                sortedColor.put(i,set1);
                i++;
            }

            Integer j=0;
            for (Map.Entry<Integer, Set<Integer>> entry : sortedColor.entrySet()){
                Integer key = entry.getKey();
                Set<Integer> value = entry.getValue();
                List<Integer> order = orders.get(key);//平行的节点
                for(Integer integer: value){  //颜色集合
                    List<Integer> list1= new ArrayList<>();
                    for(Integer node: order){ //平行的节点
                        if(colorAssignment.get(node).equals(integer)){ //颜色相同的节点
                            list1.add(node);
                            put_flag.put(node,1);
                        }
                    }
                    node_no_choose.put(j,list1);
                    j++;
                }
                for(Integer integer2: order){
                    if(!put_flag.containsKey(integer2)){
                        node_no_choose.put(j, new ArrayList<>(Arrays.asList(integer2)));
                        put_flag.put(integer2,1);
                        j++;
                    }
                }
            }
        }


        Map<Integer, List<Integer>> dictionary = new LinkedHashMap<>();
        Map<Integer, List<Integer>> field = new LinkedHashMap<>();
        Map<Integer, Integer> field_flag2 = new LinkedHashMap<>();

        int step = 0;  //要按照拓扑顺序
        for (Map.Entry<Integer, List<Integer>> entry : node_no_choose.entrySet()) {
            Integer key = entry.getKey();
            List<Integer> order = entry.getValue();
//            step++;
            List<Integer> field_1 = new ArrayList<>();
            List<Integer> field_2 = new ArrayList<>();
            List<Integer> field_3 = new ArrayList<>();
            List<Integer> field_4 = new ArrayList<>();
            for (Integer value1 : order) {
                int len1 = node_pos.get(value1).size();
                if(len1==1){
                    field_1.add(value1);
                }else if(len1==2){
                    field_2.add(value1);
                }else if(len1==3){
                    field_3.add(value1);
                }else{
                    field_4.add(value1);
                }
            }
            List<Integer> p1 = new ArrayList<>();
            if(!field_4.isEmpty()){
                p1.clear();
                for(Integer v1:field_4){
                    step++;
                    List<Integer> list1 = node_pos.get(v1);  //存储位置
                    p1.addAll(list1);
                    break;
                }
                field.put(step, new ArrayList<>(field_4));
                dictionary.put(step, new ArrayList<>(p1));
                for(Integer v1:field_4){
                    field_flag2.put(v1, 1); //表示存储了
                }
            }

            if(!field_3.isEmpty()){
                p1.clear();
                for(Integer v1:field_3){
                    step++;
                    List<Integer> list1 = node_pos.get(v1);  //存储位置
                    if (list1.get(0) % 2 == 0) { //偶数
                        p1.addAll(list1);
                        p1.add(-1); //-1占位
                    } else {  //奇数
                        p1.add(-1); //-1占位
                        p1.addAll(list1);
                    }
                    break;
                }
                field.put(step, new ArrayList<>(field_3));
                dictionary.put(step, new ArrayList<>(p1));
                for(Integer v1:field_3){
                    field_flag2.put(v1, 1); //表示存储了
                }
            }

            if(!field_2.isEmpty()){
                p1.clear();
                for(Integer v1:field_2){
                    step++;
                    List<Integer> list1 = node_pos.get(v1);  //存储位置
                    if (list1.get(0) % 2 == 0) { //偶数
                        p1.addAll(list1);
                        p1.add(-1); //-1占位
                        p1.add(-1); //-1占位
                    } else {  //奇数
                        p1.add(-1); //-1占位
                        p1.addAll(list1);
                        p1.add(-1); //-1占位
                    }
                    break;
                }
                field.put(step, new ArrayList<>(field_2));
                dictionary.put(step, new ArrayList<>(p1));
                for(Integer v1:field_2){
                    field_flag2.put(v1, 1); //表示存储了
                }
            }

            if(!field_1.isEmpty()){
                p1.clear();
                for(Integer v1:field_1){
                    step++;
                    List<Integer> list1 = node_pos.get(v1);  //存储位置
                    if (list1.get(0) % 2 == 0) { //偶数
                        p1.addAll(list1);
                        p1.add(-1); //-1占位
                        p1.add(-1); //-1占位
                        p1.add(-1); //-1占位
                    } else {  //奇数
                        p1.add(-1); //-1占位
                        p1.addAll(list1);
                        p1.add(-1); //-1占位
                        p1.add(-1); //-1占位
                    }
                    break;
                }
                field.put(step, new ArrayList<>(field_1));
                dictionary.put(step, new ArrayList<>(p1));
                for(Integer v1:field_1){
                    field_flag2.put(v1, 1); //表示存储了
                }
            }
        }
        /*
        for(int node:node_list){ //没有放置的，应该没有
            if(!field_flag2.containsKey(node)) {
                step++;
                List<Integer> p2 = new ArrayList<>();
                List<Integer> list1 = node_pos.get(node);

                if (list1.size() == 4) {
                    p2.addAll(list1);
                }
                if (list1.size() == 3) {
                    if (list1.get(0) % 2 == 0) { //偶数
                        p2.addAll(list1);
                        p2.add(-1); //-1占位
                    } else {  //奇数
                        p2.add(-1); //-1占位
                        p2.addAll(list1);
                    }
                }
                if (list1.size() == 2) {
                    if (list1.get(0) % 2 == 0) { //偶数
                        p2.addAll(list1);
                        p2.add(-1); //-1占位
                        p2.add(-1); //-1占位
                    } else {  //奇数
                        p2.add(-1); //-1占位
                        p2.addAll(list1);
                        p2.add(-1); //-1占位
                    }
                }
                if (list1.size() == 1) {
                    if (list1.get(0) % 2 == 0) { //偶数
                        p2.addAll(list1);
                        p2.add(-1); //-1占位
                        p2.add(-1); //-1占位
                        p2.add(-1); //-1占位
                    } else {  //奇数
                        p2.add(-1); //-1占位
                        p2.addAll(list1);
                        p2.add(-1); //-1占位
                        p2.add(-1); //-1占位
                    }
                }
                field.put(step, new ArrayList<>(Arrays.asList(node)));
                dictionary.put(step, new ArrayList<>(p2));
            }
        }
         */


        pathfile = Paths.get(csvPath, "output2.csv");
        writeFile2(dictionary, field, pathfile.toString());
        System.out.println(' ');
    }

    public static void writeFile2(Map<Integer, List<Integer>> dictionary, Map<Integer, List<Integer>> field, String csvPath){
        Map<Integer, List<String>> dictionary1 = new LinkedHashMap<>();
//        dictionary1.clear();
        for(Map.Entry<Integer, List<Integer>> entry: dictionary.entrySet()){
            Integer key = entry.getKey();
            List<Integer> value = entry.getValue();
            List<String> string1 = new ArrayList<>();
            for(Integer value1:value){
                if(value1!=-1){
                    string1.add(value1.toString());
                }else{
                    string1.add("-".toString());
                }
            }
            dictionary1.put(key, string1);
        }
        Map<Integer, List<String>> field1 = new LinkedHashMap<>();
//        field1.clear();
        for(Map.Entry<Integer, List<Integer>> entry2: field.entrySet()){
            Integer key = entry2.getKey();
            List<Integer> value = entry2.getValue();
            List<String> string1 = new ArrayList<>();
            for(Integer value1:value){
                string1.add(value1.toString());
            }
            field1.put(key, string1);
        }
        int len1 = dictionary1.size();

        String fileName = csvPath;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            int i=0;
            for(Map.Entry<Integer, List<String>> entry: dictionary1.entrySet()){
                i++;
                Integer key = entry.getKey();
                List<String> value = entry.getValue();
                List<String> field_value = field1.get(key);
                String csvRow = String.join(",", value)+","+String.join(",", field_value);
//                String csvRow = String.format("%s,%s", String.join(",", value), String.join(",", field_value));
//                writer.write(csvRow);
                if (!value.isEmpty() && !field_value.isEmpty() && !csvRow.trim().endsWith(",")) { // 检查csvRow是否不是空字符串，并且不以逗号结尾（避免只包含换行符的情况）
                    csvRow += "\n"; // 如果csvRow有效，则添加换行符
                    writer.write(csvRow);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFile2(Map<Integer, List<Integer>> node_pos, String csvPath2){
        String csvFile = csvPath2; // 替换为你的CSV文件路径
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        ArrayList<String[]> content = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // 使用逗号作为分隔符
                String[] values = line.split(cvsSplitBy);
                // 输出每一行的值，或者根据你的需求处理它们
                content.add(values);
                System.out.println(values[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (String[] strings : content){
            List<Integer> a1 = new ArrayList<>();
            for(int i=1;i<strings.length;i++){
                a1.add(Integer.parseInt(strings[i]));
            }
            node_pos.put(Integer.parseInt(strings[0]), a1);
        }
    }

    public static void writeFile(Map<Integer, List<String>> node_pos, String csvPath) {
        String fileName = csvPath;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Map.Entry<Integer, List<String>> entry : node_pos.entrySet()) {
                Integer key = entry.getKey();
                List<String> value = entry.getValue();
                if (!value.isEmpty()) {
                    String csvRow = String.valueOf(key) + "," + String.join(",", value) + "\n";
                    writer.write(csvRow);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFile(Graph graph, String csvPath) {
        String csvFile = csvPath; // 替换为你的CSV文件路径
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        ArrayList<String[]> content = new ArrayList<>();

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // 使用逗号作为分隔符
                String[] values = line.split(cvsSplitBy);
                // 输出每一行的值，或者根据你的需求处理它们
                content.add(values);
//                System.out.println(values[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (String[] strings : content) {  //存储节点
            node_list.add(Integer.parseInt(strings[0]));
            size_list.add(Integer.parseInt(strings[1]));
            node_size.put(Integer.parseInt(strings[0]),Integer.parseInt(strings[1]));
        }
        for (String[] strings : content) {  //存储拓扑
            for (int i = 2; i < strings.length; i++) {
                graph.addEdge(Integer.parseInt(strings[0]),
                        Integer.parseInt(strings[i]));
            }
        }
    }

}

class Graph {
    private Map<Integer, List<Integer>> adjList;
    private Map<Integer, Integer> inDegrees;
    private Map<Integer, Integer> outDegrees;
    private Set<Integer> nodes;
    private Map<Integer, Set<Integer>> conflictGraph;

    public Graph() {
        adjList = new LinkedHashMap<>();
        inDegrees = new LinkedHashMap<>();
        outDegrees = new LinkedHashMap<>();
        nodes = new HashSet<>();
        conflictGraph = new HashMap<>();

    }

    public void addEdge(int from, int to) {
        adjList.putIfAbsent(from, new ArrayList<>());
        adjList.get(from).add(to);
        inDegrees.put(to, inDegrees.getOrDefault(to, 0) + 1);
        nodes.add(from);
        nodes.add(to);
    }

    public Map<Integer, List<Integer>> getAdjList(){
        return this.adjList;
    }
    public Map<Integer, Integer> getinDegrees(){
        return this.inDegrees;
    }

    // 获取入度为0的节点集合
    public Set<Integer> getSources() {
        Set<Integer> source = new HashSet<>();
        for(Integer node:nodes){
            if(!inDegrees.containsKey(node)){
                source.add(node);
            }
        }
        return source;
    }

    // Kahn算法的拓扑排序实现
    public List<List<Integer>> topologicalSort(int V, ArrayList<Integer> size_list) {
        List<Integer> result = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        Map<Integer, Integer> inDegrees2 = new HashMap<>(inDegrees);
        List<List<Integer>> result2 = new ArrayList<>();

        // 将所有入度为0的节点加入队列
        Set<Integer> source = getSources();
        for(Integer integer:source){
            queue.offer(integer);
        }

        // 拓扑排序
        int step = 0;
        while (!queue.isEmpty()) {
            List<Integer> parallelNodes = new ArrayList<>(); // 存储当前轮可以并列的节点

            int size = queue.size(); // 当前迭代中入度为0的节点数
            int num1 = 0;
            int num2=0;
            for (Integer element : queue) {
                if(size_list.get(element)>=24)
                    num2++;
                if(size_list.get(element)==16)
                    num1++;
            }
            int[] list = {size-num1-num2, num1, num2};
            int flag=0;
//            if(num2<4 && size>4){flag=1;} //不允许弹出3、4bit
            float myFloat = (float) size;
            if(myFloat/num2>2){
                flag=1;
//                if(size-num1-num2>2*num1){flag=2;}//如果1、2比特中1很多（1>2的1.5倍），就不允许弹出2
            }
            for (int i = 0; i < size; i++) {
                int u = queue.poll();
                if(flag==1 && size_list.get(u)>=24){
                    queue.offer(u);
                    continue;
                }
//                if(flag==2 && size_list.get(u)>=16){
//                    queue.offer(u);
//                    continue;
//                }

                result.add(u); // 将节点加入结果列表
                parallelNodes.add(u); // 添加到当前轮可以并列的节点列表中

                // 遍历u的所有邻接节点
                if(adjList.containsKey(u)){
                    for (int v : adjList.get(u)) {
                        Integer a = inDegrees2.get(v);
                        a--;
                        inDegrees2.put(v,a);
//                inDegree[v]--; // 将v的入度减1
                        if (inDegrees2.get(v) == 0) { // 如果v的入度变为0，则将其加入队列
                            queue.offer(v);
                        }
                    }
                }
            }
            // 如果当前轮有可以并列的节点，则添加到结果列表中
            if (!parallelNodes.isEmpty()) {
                result2.add(parallelNodes);
            }
        }
        // 如果结果列表中的节点数量不等于图中的节点数量，则图中存在环
//        if (result.size() != V) {
//            throw new IllegalStateException("Graph contains a cycle");
//        }
        return result2;
    }

    // 使用DFS找到从入度为0的节点到出度为0的节点的所有路径
    public void findAllPaths(int start, Set<Integer> visited, List<Integer> path, List<List<Integer>> allPaths) {
        // 将当前节点添加到路径中
        path.add(start);

        // 检查当前节点是否是出度为0的节点
        if (adjList.getOrDefault(start, Collections.emptyList()).isEmpty()) {
            // 如果是，则将当前路径添加到所有路径列表中
            allPaths.add(new ArrayList<>(path));
            // 回溯，移除当前节点
            path.remove(path.size() - 1);
            return;
        }
        // 否则，继续DFS遍历
        visited.add(start);
        for (int neighbor : adjList.getOrDefault(start, Collections.emptyList())) {
            if (!visited.contains(neighbor)) {
                findAllPaths(neighbor, visited, path, allPaths);
            }
        }
        // 回溯，移除当前节点
        path.remove(path.size() - 1);
        visited.remove(start);
    }

    // 查找所有从入度为0的节点到出度为0的节点的路径
    public List<List<Integer>> findPathsFromSourceToSink() {
        List<List<Integer>> allPaths = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
//        List<Integer> path = new ArrayList<>();
        Set<Integer> sources = getSources();

        for (int source : sources) {
            List<Integer> path = new ArrayList<>();
            findAllPaths(source, visited, path, allPaths);
        }
        return allPaths;
    }

    // 构建冲突图
    public Map<Integer, Set<Integer>> buildConflictGraph(List<List<Integer>> allPaths) {
        // 初始化冲突图
        for (int node : nodes) {
            conflictGraph.put(node, new HashSet<>());
        }
        for(List<Integer> list1:allPaths){
            for(int i=0;i<list1.size()-1;i++){
                for(int j=i+1;j<list1.size();j++){
                    conflictGraph.get(list1.get(i)).add(list1.get(j));
                    conflictGraph.get(list1.get(j)).add(list1.get(i));
                }
            }
        }

//        // 遍历所有节点，为每个节点找到与其冲突的节点
//        for (int node : nodes) {
//            // 找到所有从当前节点可达的节点，并将它们标记为冲突
//            markReachableNodesAsConflict(node, node);
//        }
        return conflictGraph;
    }
    // 使用深度优先搜索来标记从起始节点可达的所有节点为冲突
    private void markReachableNodesAsConflict(int currentNode, int originalNode) {
        for (int successor : adjList.getOrDefault(currentNode, Collections.emptyList())) {
            // 如果后继节点不是原始节点，则标记为冲突
            if (successor != originalNode) {
                // 添加双向冲突关系
                conflictGraph.get(originalNode).add(successor);
                conflictGraph.get(successor).add(originalNode);
                // 递归检查，但避免重复标记
                if (!conflictGraph.get(originalNode).containsAll(conflictGraph.get(successor))) {
                    markReachableNodesAsConflict(successor, originalNode);
                }
            }
        }
    }


}


