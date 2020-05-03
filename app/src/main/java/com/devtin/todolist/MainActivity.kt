package com.devtin.todolist

import android.graphics.Paint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devtin.todolist.databinding.ActivityMainBinding
import com.devtin.todolist.databinding.ItemTodoBinding
import kotlinx.android.synthetic.main.item_todo.view.*

class MainActivity : AppCompatActivity() {
    //뷰 바인딩 적용
    private lateinit var binding: ActivityMainBinding
    private val data = arrayListOf<Todo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        data.add(Todo("청소"))
        data.add(Todo("과제", true))

        /*binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = TodoAdapter(data,
                                                    onClickDeleteIcon = {
                                                        deleteTodo(it)
                                                    }, )*/

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = TodoAdapter(data,
                onClickDeleteIcon = {
                    deleteTodo(it)
                }, onClickItem = {
                    toggleTodo(it)
                })
        }

        binding.addBtn.setOnClickListener {
            addTodo()
        }

    }

    fun addTodo(){
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
    }

}

// (data) getter setter 가 구현 됨
data class Todo(
    val text: String,
    var isDone: Boolean = false
)

//recyclerView 를 연결해줌
class TodoAdapter(private val myDataset: List<Todo>,
                  val onClickDeleteIcon: (todo: Todo) -> Unit,
                  val onClickItem: (todo: Todo) -> Unit
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
        holder.binding.todoText.text = todo.text

        if(todo.isDone){
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
}

