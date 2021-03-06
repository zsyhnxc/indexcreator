package net.zsy.indexcreator.product;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.zsy.indexcreator.dispatch.AsyncIndexHandler;
import net.zsy.indexcreator.dispatch.IndexHandler;
import net.zsy.indexcreator.indexbean.Product;

public class ProductIndexHandler extends AsyncIndexHandler implements IndexHandler<ProductIndex> {

	public ProductIndexHandler(BlockingQueue<String> queue) {

		super(queue);

		Thread t = new Thread(new HandleThread());
		t.start();

	}

	private BlockingQueue<ProductIndex> asyncqueue = new LinkedBlockingQueue<ProductIndex>();

	@Override
	public void handle(ProductIndex t) {
		asyncqueue.add(t);
	}

	private class HandleThread implements Runnable {

		private ExecutorService exec = Executors.newFixedThreadPool(5);

		private ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					final ProductIndex index = asyncqueue.take();
					exec.submit(new Runnable() {

						@Override
						public void run() {
							// 调用索引文档创建方法
							Product p = new Product();
							p.setId(index.getObjId());
							try {
								String jsonstring = objectMapper.writeValueAsString(p);
								queue.put(jsonstring);
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}

	}
}
