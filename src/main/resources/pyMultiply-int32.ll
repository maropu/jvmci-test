; ModuleID = 'pyMultiply-int32.bc'
source_filename = "<string>"
target datalayout = "e-m:o-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-apple-darwin16.7.0"

@"_ZN08NumbaEnv8__main__14pyMultiply$242Eii" = common local_unnamed_addr global i8* null

; Function Attrs: norecurse nounwind writeonly
define i32 @"_ZN8__main__14pyMultiply$242Eii"(i32* noalias nocapture %retptr, { i8*, i32 }** noalias nocapture readnone %excinfo, i32 %arg.a, i32 %arg.b) local_unnamed_addr #0 {
entry:
  %.16 = mul i32 %arg.b, %arg.a
  store i32 %.16, i32* %retptr, align 4
  ret i32 0
}

; Function Attrs: norecurse nounwind readnone
define i32 @"cfunc._ZN8__main__14pyMultiply$242Eii"(i32 %.1, i32 %.2) local_unnamed_addr #1 {
entry:
  %.16.i = mul i32 %.2, %.1
  ret i32 %.16.i
}

attributes #0 = { norecurse nounwind writeonly }
attributes #1 = { norecurse nounwind readnone }
