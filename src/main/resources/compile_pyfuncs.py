from numba import cfunc

# A helper function to write a python function as LLVM bitcode
def write_pyfunc_as_bitcode(pyfunc, sig, filename_suffix=""):
  with open(pyfunc.__name__ + filename_suffix + ".bc", "wb") as fout:
    f = cfunc(sig)(pyfunc)
    fout.write(f._library._final_module.as_bitcode())

def pyAdd(a, b):
  return a + b;

def pyMultiply(a, b):
  return a * b;

write_pyfunc_as_bitcode(pyAdd, "int32(int32, int32)", "-int32")
write_pyfunc_as_bitcode(pyMultiply, "int32(int32, int32)", "-int32")

