package by.nalivajr.yanaproject.sample.ui

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.widget.*
import by.nalivajr.anuta.annonatations.ui.AutoActivity
import by.nalivajr.anuta.annonatations.ui.InnerView
import by.nalivajr.anuta.components.adapters.data.binder.DataBinder
import by.nalivajr.anuta.components.adapters.data.provider.AbstractDataProvider
import by.nalivajr.anuta.tools.Anuta
import by.nalivajr.yana.models.Message
import by.nalivajr.yana.tools.MessageBuilder
import by.nalivajr.yana.transmission.MessageReceiver
import by.nalivajr.yana.transmission.MessageSender
import by.nalivajr.yana.transmission.YanaMessageReceiver
import by.nalivajr.yana.transmission.YanaMessageSender
import by.nalivajr.yanaproject.R

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
@AutoActivity(recursive = false, layoutId = R.layout.ac_sample)
class SampleActivity : Activity() {

    private companion object {
        val SENDER_ID = "yana-sample-sender"
        val FIRST_RECEIVER_ID = "yana-sample-receiver-1"
        val SECOND_RECEIVER_ID = "yana-sample-receiver-2"
        val AUTHORITY = "YanaSampleAuthority"
    }

    @InnerView(R.id.et_action) private lateinit var editAction : EditText;
    @InnerView(R.id.et_payload) private lateinit var editPayload : EditText;
    @InnerView(R.id.btn_send_first) private lateinit var sendFirst : Button;
    @InnerView(R.id.btn_send_second) private lateinit var sendSecond : Button;
    @InnerView(R.id.btn_send_both) private lateinit var sendBoth : Button;
    @InnerView(R.id.lv_first) private lateinit var firstReceivedList: ListView;
    @InnerView(R.id.lv_second) private lateinit var secondReceivedList: ListView;

    private lateinit var receiverFirst : MessageReceiver;
    private lateinit var receiverSecond : MessageReceiver;
    private lateinit var sender : MessageSender;

    private lateinit var uiNotifier : Handler;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Anuta.viewTools.setContentView(this)

        uiNotifier = Handler(mainLooper);

        val dataBinder = DataBinder<Message<String>> {
            view, itemLayoutId, viewId, msg ->
                when(viewId) {
                    R.id.tv_msg_id -> (view as TextView).text = msg.messageId.toString();
                    R.id.tv_command -> (view as TextView).text = msg.command;
                    R.id.tv_groip_id -> (view as TextView).text = msg.groupId;
                    R.id.tv_order -> (view as TextView).text = msg.order.toString();
                    R.id.tv_payload -> (view as TextView).text = msg.payload;
                    R.id.tv_receiver_id -> (view as TextView).text = msg.recipientId;
                    R.id.tv_sent_date -> (view as TextView).text = msg.creationDate.toString();
                    R.id.tv_sender_id -> (view as TextView).text = msg.senderId.toString();
                }
        }

        val firstMessages = mutableListOf<Message<String>>();
        val secondMessages = mutableListOf<Message<String>>();

        val firstDataProvider = object: AbstractDataProvider<Message<String>>() {
            override fun getItem(p0: Int): Message<String>? = firstMessages[p0]
            override fun count(): Int = firstMessages.size;
        }
        val secondDataProvider = object: AbstractDataProvider<Message<String>>() {
            override fun getItem(p0: Int): Message<String>? = secondMessages[p0]
            override fun count(): Int = secondMessages.size;
        }

        val firstReceiverAdapter = Anuta.adapterTools.buildAdapter(this@SampleActivity, firstDataProvider, dataBinder, R.layout.layout_message);
        val secondReceiverAdapter = Anuta.adapterTools.buildAdapter(this@SampleActivity, secondDataProvider, dataBinder, R.layout.layout_message);
        firstReceivedList.adapter = firstReceiverAdapter;
        secondReceivedList.adapter = secondReceiverAdapter;

        initCommunicationServices(firstMessages, secondMessages, firstReceiverAdapter, secondReceiverAdapter);

        sendFirst.setOnClickListener({ sendMessageTo(FIRST_RECEIVER_ID) })

        sendSecond.setOnClickListener({ sendMessageTo(SECOND_RECEIVER_ID) })

        sendBoth.setOnClickListener({ sendMessageTo(null) })
    }

    private fun initCommunicationServices(firstMessages: MutableList<Message<String>>,
                                          secondMessages: MutableList<Message<String>>,
                                          firstReceiverAdapter: BaseAdapter,
                                          secondReceiverAdapter: BaseAdapter) {
        sender = YanaMessageSender(this, SENDER_ID, AUTHORITY);
        receiverFirst = object: YanaMessageReceiver(this@SampleActivity, AUTHORITY, receiverId = FIRST_RECEIVER_ID) {
            override fun onReceive(message: Message<String>) {
                firstMessages.add(message)
                uiNotifier.post { firstReceiverAdapter.notifyDataSetChanged() }
            }
        }
        receiverSecond = object: YanaMessageReceiver(this@SampleActivity, AUTHORITY, receiverId = SECOND_RECEIVER_ID) {
            override fun onReceive(message: Message<String>) {
                secondMessages.add(message)
                uiNotifier.post { secondReceiverAdapter.notifyDataSetChanged() }
            }
        }

        sender.start();
        receiverFirst.start();
        receiverSecond.start();
    }

    private fun sendMessageTo(receiverId: String?) {
        val action = editAction.text.toString();
        val payload = editPayload.text.toString();
        val msg: Message<String> = MessageBuilder.obtainMessageBuilderWithPayload(payload)
                .command(action)
                .recipient(receiverId)
                .build()

        sender.sendOnMyBehalf(msg)
    }

    override fun onDestroy() {
        super.onDestroy()
        sender.stop();
        receiverFirst.stop()
        receiverSecond.stop()
    }
}