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

        var temp = "" + CommonVariables.puzzlesSolved
        binding.puzzlesSolvedCountTextView.text = temp

        temp = "" + CommonVariables.fourPiecePuzzleSolvedCount
        binding.twoXtwoPuzzlesSolvedTextView.text = temp

        temp = "" + CommonVariables.fourRecordSolveTime / 1000.0 + " sec."
        binding.twoXtwoSolveTimeTextView.text = temp

        temp = "" + CommonVariables.ninePiecePuzzleSolvedCount
        binding.threeXthreePuzzlesSolvedTextView.text = temp

        temp = "" + CommonVariables.nineRecordSolveTime / 1000.0 + " sec."
        binding.threeXthreeSolveTimeTextView.text = temp

        temp = "" + CommonVariables.sixteenPiecePuzzleSolvedCount
        binding.fourXfourPuzzlesSolvedTextView.text = temp

        temp = "" + CommonVariables.sixteenRecordSolveTime / 1000.0 + " sec."
        binding.fourXfourSolveTimeTextView.text = temp

        temp = "" + CommonVariables.twentyfivePiecePuzzleSolvedCount
        binding.fiveXfivePuzzlesSolvedTextView.text = temp

        temp = "" + CommonVariables.twentyfiveRecordSolveTime / 1000.0 + " sec."
        binding.fiveXfiveSolveTimeTextView.text = temp

        temp = "" + CommonVariables.thirtysixPiecePuzzleSolvedCount
        binding.sixXsixPuzzlesSolvedTextView.text = temp

        temp = "" + CommonVariables.thirtysixRecordsSolveTime / 1000.0 + " sec."
        binding.sixXsixSolveTimeTextView.text = temp

        temp = "" + CommonVariables.fourtyninePiecePuzzleSolvedCount
        binding.sevenXsevenPuzzlesSovedTextView.text = temp

        temp = "" + CommonVariables.fourtynineRecordsSolveTime / 1000.0 + " sec."
        binding.sevenXsevenSolveTimeTextView.text = temp

        temp = "" + CommonVariables.imagesSaved
        binding.imageSavedTextView.text = temp

        temp = "" + CommonVariables.blogLinksTraversed
        binding.blogLinksTraversedTextView.text = temp

        temp = "" + CommonVariables.musicSaved
        binding.musicSavedTextView.text = temp
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}