; ModuleID = 'pyAdd-float64.bc'
source_filename = "<string>"
target datalayout = "e-m:o-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-apple-darwin16.7.0"

@"_ZN08NumbaEnv8__main__9pyAdd$242Edd" = common local_unnamed_addr global i8* null

; Function Attrs: norecurse nounwind writeonly
define i32 @"_ZN8__main__9pyAdd$242Edd"(double* noalias nocapture %retptr, { i8*, i32 }** noalias nocapture readnone %excinfo, double %arg.a, double %arg.b) local_unnamed_addr #0 {
entry:
  %.14 = fadd double %arg.a, %arg.b
  store double %.14, double* %retptr, align 8
  ret i32 0
}

; Function Attrs: norecurse nounwind readnone
define double @"cfunc._ZN8__main__9pyAdd$242Edd"(double %.1, double %.2) local_unnamed_addr #1 {
entry:
  %.14.i = fadd double %.1, %.2
  ret double %.14.i
}

attributes #0 = { norecurse nounwind writeonly }
attributes #1 = { norecurse nounwind readnone }
