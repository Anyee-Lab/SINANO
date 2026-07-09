package com.example.huihutong

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.huihutong.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = PrefsHelper.getInstance(requireContext())
        binding.openIdEdit.setText(prefs.openId ?: "")

        binding.saveButton.setOnClickListener {
            val openId = binding.openIdEdit.text.toString().trim()
            if (openId.isBlank()) {
                binding.openIdEdit.error = getString(R.string.hint_openid)
                return@setOnClickListener
            }
            prefs.openId = openId
            binding.statusText.setText(R.string.status_config_saved)
            binding.statusText.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
