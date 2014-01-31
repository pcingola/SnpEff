#-------------------------------------------------------------------------------
#
# Process data from Epigenome roadmap
#
#-------------------------------------------------------------------------------

macs="/sb/programs/diabetes_analysis/tools/MACS-1.4.2/bin/macs14"
dir=`pwd -P`"/sample-experiment"
macsDir=`pwd -P`"/macs"

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

#---
# Run peak finding algorithm
#---
for pair in \
            Adult_Liver/Histone_H3K27me3/BI.Adult_Liver.H3K27me3.3.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.3.bed.gz \
            Adult_Liver/Histone_H3K27me3/BI.Adult_Liver.H3K27me3.5.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.5.bed.gz \
            Adult_Liver/Histone_H3K36me3/BI.Adult_Liver.H3K36me3.3.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.3.bed.gz \
            Adult_Liver/Histone_H3K36me3/BI.Adult_Liver.H3K36me3.4.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.4.bed.gz \
            Adult_Liver/Histone_H3K36me3/BI.Adult_Liver.H3K36me3.5.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.5.bed.gz \
            Adult_Liver/Histone_H3K4me1/BI.Adult_Liver.H3K4me1.3.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.3.bed.gz \
            Adult_Liver/Histone_H3K4me1/BI.Adult_Liver.H3K4me1.4.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.4.bed.gz \
            Adult_Liver/Histone_H3K4me1/BI.Adult_Liver.H3K4me1.5.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.5.bed.gz \
            Adult_Liver/Histone_H3K4me3/BI.Adult_Liver.H3K4me3.3.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.3.bed.gz \
            Adult_Liver/Histone_H3K4me3/BI.Adult_Liver.H3K4me3.4.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.4.bed.gz \
            Adult_Liver/Histone_H3K4me3/BI.Adult_Liver.H3K4me3.5.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.5.bed.gz \
            Adult_Liver/Histone_H3K9ac/BI.Adult_Liver.H3K9ac.3.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.3.bed.gz \
            Adult_Liver/Histone_H3K9ac/BI.Adult_Liver.H3K9ac.4.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.4.bed.gz \
            Adult_Liver/Histone_H3K9me3/BI.Adult_Liver.H3K9me3.3.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.3.bed.gz \
            Adult_Liver/Histone_H3K9me3/BI.Adult_Liver.H3K9me3.4.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.4.bed.gz \
            Adult_Liver/Histone_H3K9me3/BI.Adult_Liver.H3K9me3.5.bed.gz,Adult_Liver/ChIP-Seq_Input/BI.Adult_Liver.Input.5.bed.gz \
            Adipose_Tissue/Histone_H3K27ac/UCSD.Adipose_Tissue.H3K27ac.STL003.bed.gz,Adipose_Tissue/ChIP-Seq_Input/UCSD.Adipose_Tissue.Input.STL003.bed.gz \
			Pancreatic_Islets/Histone_H3K27me3/BI.Pancreatic_Islets.H3K27me3.pancreatic_islets_normal_3_27_09.bed.gz,Pancreatic_Islets/ChIP-Seq_Input/BI.Pancreatic_Islets.Input.pancreatic_islets_normal_3_27_09.bed.gz \
			Pancreatic_Islets/Histone_H3K36me3/BI.Pancreatic_Islets.H3K36me3.pancreatic_islets_normal_3_27_09.bed.gz,Pancreatic_Islets/ChIP-Seq_Input/BI.Pancreatic_Islets.Input.pancreatic_islets_normal_3_27_09.bed.gz \
			Pancreatic_Islets/Histone_H3K4me1/BI.Pancreatic_Islets.H3K4me1.pancreatic_islets_normal_3_27_09.bed.gz,Pancreatic_Islets/ChIP-Seq_Input/BI.Pancreatic_Islets.Input.pancreatic_islets_normal_3_27_09.bed.gz \
			Pancreatic_Islets/Histone_H3K4me3/BI.Pancreatic_Islets.H3K4me3.pancreatic_islets_normal_3_27_09.bed.gz,Pancreatic_Islets/ChIP-Seq_Input/BI.Pancreatic_Islets.Input.pancreatic_islets_normal_3_27_09.bed.gz \
			Pancreatic_Islets/Histone_H3K9ac/BI.Pancreatic_Islets.H3K9ac.pancreatic_islets_normal_3_27_09.bed.gz,Pancreatic_Islets/ChIP-Seq_Input/BI.Pancreatic_Islets.Input.pancreatic_islets_normal_3_27_09.bed.gz \
			Pancreatic_Islets/Histone_H3K9me3/BI.Pancreatic_Islets.H3K9me3.pancreatic_islets_normal_0_0_00.bed.gz,Pancreatic_Islets/ChIP-Seq_Input/BI.Pancreatic_Islets.Input.pancreatic_islets_normal_3_27_09.bed.gz \
            Skeletal_Muscle/Histone_H3K27ac/BI.Skeletal_Muscle.H3K27ac.62.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.62.bed.gz \
            Skeletal_Muscle/Histone_H3K27me3/BI.Skeletal_Muscle.H3K27me3.19.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.19.bed.gz \
            Skeletal_Muscle/Histone_H3K27me3/BI.Skeletal_Muscle.H3K27me3.62.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.62.bed.gz \
            Skeletal_Muscle/Histone_H3K27me3/BI.Skeletal_Muscle.H3K27me3.63.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.63.bed.gz \
            Skeletal_Muscle/Histone_H3K36me3/BI.Skeletal_Muscle.H3K36me3.19.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.19.bed.gz \
            Skeletal_Muscle/Histone_H3K36me3/BI.Skeletal_Muscle.H3K36me3.62.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.62.bed.gz \
            Skeletal_Muscle/Histone_H3K36me3/BI.Skeletal_Muscle.H3K36me3.63.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.63.bed.gz \
            Skeletal_Muscle/Histone_H3K4me1/BI.Skeletal_Muscle.H3K4me1.19.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.19.bed.gz \
            Skeletal_Muscle/Histone_H3K4me1/BI.Skeletal_Muscle.H3K4me1.62.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.62.bed.gz \
            Skeletal_Muscle/Histone_H3K4me1/BI.Skeletal_Muscle.H3K4me1.63.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.63.bed.gz \
            Skeletal_Muscle/Histone_H3K4me3/BI.Skeletal_Muscle.H3K4me3.19.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.19.bed.gz \
            Skeletal_Muscle/Histone_H3K4me3/BI.Skeletal_Muscle.H3K4me3.62.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.62.bed.gz \
            Skeletal_Muscle/Histone_H3K4me3/BI.Skeletal_Muscle.H3K4me3.63.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.63.bed.gz \
            Skeletal_Muscle/Histone_H3K9ac/BI.Skeletal_Muscle.H3K9ac.19.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.19.bed.gz \
            Skeletal_Muscle/Histone_H3K9ac/BI.Skeletal_Muscle.H3K9ac.62.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.62.bed.gz \
            Skeletal_Muscle/Histone_H3K9ac/BI.Skeletal_Muscle.H3K9ac.63.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.63.bed.gz \
            Skeletal_Muscle/Histone_H3K9me3/BI.Skeletal_Muscle.H3K9me3.19.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.19.bed.gz \
            Skeletal_Muscle/Histone_H3K9me3/BI.Skeletal_Muscle.H3K9me3.62.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.62.bed.gz \
            Skeletal_Muscle/Histone_H3K9me3/BI.Skeletal_Muscle.H3K9me3.63.bed.gz,Skeletal_Muscle/ChIP-Seq_Input/BI.Skeletal_Muscle.Input.63.bed.gz
do
    cs="$dir/"`echo $pair | cut -f 1 -d , | tr -d "\n"`
    input=$dir/`echo $pair | cut -f 2 -d , | tr -d "\n"`
    name=$macsDir/`basename $cs .bed.gz`

    echo $name
    echo $macs -t $cs -c $input -n $name -g hs | qsub
done

#---
# Intersect and create "consensus" peaks
#---

snpsift="java -Xmx1G -jar $HOME/snpEff/SnpSift.jar"
minOverlap=10
macs="macs/peaks/"
int2="$snpsift intersect -v -minOverlap $minOverlap -cluster 2 -intersect "
int3="$snpsift intersect -v -minOverlap $minOverlap -cluster 3 -intersect "

$int2 $macs/BI.Adult_Liver.H3K27me3.3_peaks.bed $macs/BI.Adult_Liver.H3K27me3.5_peaks.bed   > regulation.Adult_Liver.H3K27me3.peaks.bed
$int2 $macs/BI.Adult_Liver.H3K9ac.3_peaks.bed $macs/BI.Adult_Liver.H3K9ac.4_peaks.bed       > regulation.Adult_Liver.H3K9ac.peaks.bed

$int3 $macs/BI.Adult_Liver.H3K36me3.3_peaks.bed $macs/BI.Adult_Liver.H3K36me3.4_peaks.bed $macs/BI.Adult_Liver.H3K36me3.5_peaks.bed                   > regulation.Adult_Liver.H3K36me3.peaks.bed
$int3 $macs/BI.Adult_Liver.H3K4me1.3_peaks.bed $macs/BI.Adult_Liver.H3K4me1.4_peaks.bed $macs/BI.Adult_Liver.H3K4me1.5_peaks.bed                      > regulation.Adult_Liver.H3K4me1.peaks.bed
$int3 $macs/BI.Adult_Liver.H3K4me3.3_peaks.bed $macs/BI.Adult_Liver.H3K4me3.4_peaks.bed $macs/BI.Adult_Liver.H3K4me3.5_peaks.bed                      > regulation.Adult_Liver.H3K4me3.peaks.bed
$int3 $macs/BI.Adult_Liver.H3K9me3.3_peaks.bed $macs/BI.Adult_Liver.H3K9me3.4_peaks.bed $macs/BI.Adult_Liver.H3K9me3.5_peaks.bed                      > regulation.Adult_Liver.H3K9me3.peaks.bed
$int3 $macs/BI.Skeletal_Muscle.H3K27me3.19_peaks.bed $macs/BI.Skeletal_Muscle.H3K27me3.62_peaks.bed $macs/BI.Skeletal_Muscle.H3K27me3.63_peaks.bed    > regulation.Skeletal_Muscle.H3K27me3.peaks.bed
$int3 $macs/BI.Skeletal_Muscle.H3K36me3.19_peaks.bed $macs/BI.Skeletal_Muscle.H3K36me3.62_peaks.bed $macs/BI.Skeletal_Muscle.H3K36me3.63_peaks.bed    > regulation.Skeletal_Muscle.H3K36me3.peaks.bed
$int3 $macs/BI.Skeletal_Muscle.H3K4me1.19_peaks.bed $macs/BI.Skeletal_Muscle.H3K4me1.62_peaks.bed $macs/BI.Skeletal_Muscle.H3K4me1.63_peaks.bed       > regulation.Skeletal_Muscle.H3K4me1.peaks.bed
$int3 $macs/BI.Skeletal_Muscle.H3K4me3.19_peaks.bed $macs/BI.Skeletal_Muscle.H3K4me3.62_peaks.bed $macs/BI.Skeletal_Muscle.H3K4me3.63_peaks.bed       > regulation.Skeletal_Muscle.H3K4me3.peaks.bed
$int3 $macs/BI.Skeletal_Muscle.H3K9ac.19_peaks.bed $macs/BI.Skeletal_Muscle.H3K9ac.62_peaks.bed $macs/BI.Skeletal_Muscle.H3K9ac.63_peaks.bed          > regulation.Skeletal_Muscle.H3K9ac.peaks.bed
$int3 $macs/BI.Skeletal_Muscle.H3K9me3.19_peaks.bed $macs/BI.Skeletal_Muscle.H3K9me3.62_peaks.bed $macs/BI.Skeletal_Muscle.H3K9me3.63_peaks.bed       > regulation.Skeletal_Muscle.H3K9me3.peaks.bed
