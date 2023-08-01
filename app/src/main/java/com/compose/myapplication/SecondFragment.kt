package com.compose.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.compose.myapplication.databinding.FragmentSecondBinding


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    companion object{
        const val TAG = "com.updated.puzzles.PuzzleStatsFragment"
    }

    var common = CommonVariables.getInstance()

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        updatePuzzleStats();
    }

    fun updatePuzzleStats() {

        var temp = "" + common.puzzlesSolved
        binding.puzzlesSolvedCountTextView.text = temp

        temp = "" + common.fourPiecePuzzleSolvedCount
        binding.twoXtwoPuzzlesSolvedTextView.text = temp

        temp = "" + common.fourRecordSolveTime / 1000.0 + " sec."
        binding.twoXtwoSolveTimeTextView.text = temp

        temp = "" + common.ninePiecePuzzleSolvedCount
        binding.threeXthreePuzzlesSolvedTextView.text = temp

        temp = "" + common.nineRecordSolveTime / 1000.0 + " sec."
        binding.threeXthreeSolveTimeTextView.text = temp

        temp = "" + common.sixteenPiecePuzzleSolvedCount
        binding.fourXfourPuzzlesSolvedTextView.text = temp

        temp = "" + common.sixteenRecordSolveTime / 1000.0 + " sec."
        binding.fourXfourSolveTimeTextView.text = temp

        temp = "" + common.twentyfivePiecePuzzleSolvedCount
        binding.fiveXfivePuzzlesSolvedTextView.text = temp

        temp = "" + common.twentyfiveRecordSolveTime / 1000.0 + " sec."
        binding.fiveXfiveSolveTimeTextView.text = temp

        temp = "" + common.thirtysixPiecePuzzleSolvedCount
        binding.sixXsixPuzzlesSolvedTextView.text = temp

        temp = "" + common.thirtysixRecordsSolveTime / 1000.0 + " sec."
        binding.sixXsixSolveTimeTextView.text = temp

        temp = "" + common.fourtyninePiecePuzzleSolvedCount
        binding.sevenXsevenPuzzlesSovedTextView.text = temp

        temp = "" + common.fourtynineRecordsSolveTime / 1000.0 + " sec."
        binding.sevenXsevenSolveTimeTextView.text = temp

        temp = "" + common.imagesSaved
        binding.imageSavedTextView.text = temp

        temp = "" + common.blogLinksTraversed
        binding.blogLinksTraversedTextView.text = temp

        temp = "" + common.musicSaved
        binding.musicSavedTextView.text = temp
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}