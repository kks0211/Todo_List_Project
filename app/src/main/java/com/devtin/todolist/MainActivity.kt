package com.devtin.todolist

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devtin.todolist.databinding.ActivityMainBinding
import com.devtin.todolist.databinding.ItemTodoBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    //뷰 바인딩 적용
    private lateinit var binding: ActivityMainBinding
    //private val data = arrayListOf<Todo>()
    //뷰모델 (핸드폰을 회전 시켰을때 초기화 되는것을 방지)
    private val viewModel: MainViewModel by viewModels()

    val RC_SIGN_IN = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //로그인이 안 됨
       if(FirebaseAuth.getInstance().currentUser == null){
           login()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = TodoAdapter(data,
                                                    onClickDeleteIcon = {
                                                        deleteTodo(it)
                                                    }, )*/

        /*binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = TodoAdapter(data,
                onClickDeleteIcon = {
                    deleteTodo(it)
                }, onClickItem = {
                    toggleTodo(it)
                })
        }*/

        /*binding.addBtn.setOnClickListener {
            addTodo(todo)
        }*/

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = TodoAdapter(
                emptyList(),
                onClickDeleteIcon = {
                    viewModel.deleteTodo(it)
                    //binding.recyclerView.adapter?.notifyDataSetChanged()
                }, onClickItem = {
                    viewModel.toggleTodo(it)
                    //binding.recyclerView.adapter?.notifyDataSetChanged()

                })
        }

        binding.addBtn.setOnClickListener {
            val todo = Todo(binding.editText.text.toString())
            viewModel.addTodo(todo)
            binding.recyclerView.adapter?.notifyDataSetChanged()
        }

        // 관찰 UI 업데이트
        viewModel.todoLiveData.observe(this, Observer {
            (binding.recyclerView.adapter as TodoAdapter).setData(it)
        })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                //val user = FirebaseAuth.getInstance().currentUser
                viewModel.fetchData()
            } else {
                //로그인 실패
                finish()
            }
        }
    }

    /*fun addTodo(){
        val todo = Todo(binding.editText.text.toString())
        data.add(todo)
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    fun deleteTodo(todo : Todo){
        data.remove(todo)
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun toggleTodo(todo: Todo) {
        todo.isDone = !todo.isDone
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }*/

    fun login(){
        var providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), RC_SIGN_IN
        )
    }

    fun logout(){
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                login()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }


}

// (data) getter setter 가 구현 됨
data class Todo(
    val text: String,
    var isDone: Boolean = false
)

//recyclerView 를 연결해줌
class TodoAdapter(//private var myDataset: List<Todo>,
                    private var myDataset: List<DocumentSnapshot>,
                  val onClickDeleteIcon: (todo: DocumentSnapshot) -> Unit,
                  val onClickItem: (todo: DocumentSnapshot) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    //class TodoViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    class TodoViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): TodoAdapter.TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)

        return TodoViewHolder(ItemTodoBinding.bind(view))
    }
    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        //val textView = holder.view.findViewById<TextView>(R.id.todo_text)
        //textView.text = myDataset[position].text

        val todo = myDataset[position]
        //holder.binding.todoText.text = todo.text
        holder.binding.todoText.text = todo.getString("text") ?: ""

        if((todo.getBoolean("isDone")?: false) == true){
            //holder.binding.todoText.paintFlags =
            // holder.binding.todoText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding.todoText.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                setTypeface(null, Typeface.ITALIC)
            }
        } else {
            holder.binding.todoText.apply {
                paintFlags = 0
                setTypeface(null, Typeface.NORMAL)
            }
        }

        holder.binding.deleteImageView.setOnClickListener {
            onClickDeleteIcon.invoke(todo)
        }

        holder.binding.root.setOnClickListener {
            onClickItem.invoke(todo)
        }

    }
    override fun getItemCount() = myDataset.size

    //데이터 변경 했을때
    fun setData(newData : List<DocumentSnapshot>){
        myDataset = newData
        notifyDataSetChanged()
    }
}

class MainViewModel : ViewModel() {
    val db = Firebase.firestore

    //liveData
    //val todoLiveData = MutableLiveData<List<Todo>>()
    val todoLiveData = MutableLiveData<List<DocumentSnapshot>>()

    init {
        fetchData()
    }

    //private val data = arrayListOf<Todo>()
    //private val data = arrayListOf<QueryDocumentSnapshot>()

    fun addTodo(todo: Todo) {
        //실제 DB에 저장
        FirebaseAuth.getInstance().currentUser?.let { user ->
            db.collection(user.uid)
                .add(todo)
        }
    }

    fun deleteTodo(todo: DocumentSnapshot) {
        //data.remove(todo)
        //todoLiveData.value = data
        FirebaseAuth.getInstance().currentUser?.let { user ->
            db.collection(user.uid).document(todo.id)
                .delete()
        }
    }
        fun toggleTodo(todo: DocumentSnapshot) {
            //todo.isDone = !todo.isDone
            //todoLiveData.value = data
            FirebaseAuth.getInstance().currentUser?.let { user ->
                val isDone = todo.getBoolean("isDone") ?: false
                db.collection(user.uid).document(todo.id)
                    .update("isDone", !isDone )
            }
        }

        fun fetchData() {
            //db.collection("todos")
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                db.collection(user.uid)
                    .addSnapshotListener { value, e ->
                        if (e != null) {
                            return@addSnapshotListener
                        }
                        if (value != null) {
                            todoLiveData.value = value.documents
                        }

                        //data.clear()
                        // for (document in value!!) {
                        /*val todo = Todo(
                            document.getString("text") ?: "",
                            document.getBoolean("isDone") ?: false
                            )*/
                        //data.add(todo)
                        //   data.add(document)
                        //}
                        //todoLiveData.value = data
                        //todoLiveData.value = value.documents

                    }
                /* .get()
                .addOnSuccessListener { result ->
                    data.clear()
                    for (document in result) {
                        val todo = Todo(
                            document.data["text"] as String,
                            document.data["isDone"] as Boolean
                        )
                        data.add(todo)
                    }
                    todoLiveData.value = data
                }
                .addOnFailureListener { exception ->
                }*/
            }
        }
    }

