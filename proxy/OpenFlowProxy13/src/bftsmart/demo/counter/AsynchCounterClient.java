/**
Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package bftsmart.demo.counter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;

/**
 * Example client that updates a BFT replicated service (a counter).
 * 
 * @author alysson
 */
public class AsynchCounterClient {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java ... CounterClient <process id> <increment> [<number of operations>]");
            System.out.println("       if <increment> equals 0 the request will be read-only");
            System.out.println("       default <number of operations> equals 1000");
            System.exit(-1);
        }

        AsynchServiceProxy counterProxy = new AsynchServiceProxy(Integer.parseInt(args[0]));
        
        try {

            int inc = Integer.parseInt(args[1]);
            int numberOfOps = (args.length > 2) ? Integer.parseInt(args[2]) : 1000;

            for (int i = 0; i < numberOfOps; i++) {

                ByteArrayOutputStream out = new ByteArrayOutputStream(4);
                new DataOutputStream(out).writeInt(inc);

                System.out.print("Invocation " + i);
                if(inc==0) {
               
                	counterProxy.invokeAsynchRequest(out.toByteArray(), new CounterReplyListener(counterProxy),TOMMessageType.UNORDERED_REQUEST);

                }else {
                        
                	counterProxy.invokeAsynchRequest(out.toByteArray(), new CounterReplyListener(counterProxy),TOMMessageType.UNORDERED_REQUEST);
                	System.out.println("Request sent");
                }     
                
            
            }
        } catch(IOException | NumberFormatException e){
            counterProxy.close();
        } 
    }
}
