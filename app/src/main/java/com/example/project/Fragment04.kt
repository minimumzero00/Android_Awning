package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.project.databinding.Fragment04Binding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Fragment04.newInstance] factory method to
 * create an instance of this fragment.
 */
class Fragment04 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: Fragment04Binding? = null //(추가)
    private val binding get() = _binding!!  //(추가)

override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_04, container, false)

        //@@@@@@@@@@ GoBluetooth 버튼 열결 시작 @@@@@@@@@@@
        val view: View = inflater.inflate(R.layout.fragment_04, container, false)
        val mbtnGoBluetooth: Button = view.findViewById(R.id.btnGoBluetooth) //객체생성
        mbtnGoBluetooth.setOnClickListener {
            activity?.let{
                val intent = Intent(context, Bluetooth::class.java)
                startActivity(intent)
            }
        }
        //@@@@@@@@@@ GoBluetooth 버튼 열결 끝 @@@@@@@@@@@

        _binding = Fragment04Binding.inflate(inflater, container, false) //(추가)
        //return binding.root//(추가)
        return view//(수정)
    }

    override fun onDestroyView() { //(추가)
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { //(추가)
        binding.textView.text = param1
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Fragment04.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
            Fragment04().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}