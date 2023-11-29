import random

P_LIST = [
    "1", "2", "3",
    "x", "y", "z", "2*x", "3*y",
    "x+y", "y+z", "x+2*y", "2*(x+y)",
    "x+y+z", "x+y+2*z", "2*x+3*(y+z)",
    "x*x", "x*x*x", "x*x*y", "x*x*(y+1)", "x*x+y*x+z*z", "x*x+z*z",
    "(x+1)*(y+1)*(z+2)", "(x+y)*(y+z)", "(x+y+z)*(x+3)+2",
    "x if x==1 else y", "y+2 if y<x else 3", "x*y if not z+y<2 else y+1",
    "2*x if x==3 and z<x else z+y", "z*z if x==1 or y==z else y",
    "y+2*x if (not (x<2)) and (y==1 or z<y) else x*x",
    "y+2 if y<x*x else 3", "z*z if x==3+1 or y==x+3*z else y",
    "2*x+(2+3*2)*(y+z)", "(x+2*3+1)*(y+1)*(z+2*2*2)",
    "2+3*x if x<3*3+2*2*3+1 and z<x else z+(2+3)*y",
    "x if x<y else y+1 if y<z else z*2"
]

EXAMPLE_COUNT = 10

def gen_benchmarks():
    for i in range(len(P_LIST)):
        f = open(f"benchmark/benchmark{i + 1}.txt", "w")
        for j in range(EXAMPLE_COUNT):
            x = random.randint(-10, 20)
            y = random.randint(-10, 20)
            z = random.randint(-10, 20)
            f.write(f"x={x}, y={y}, z={z} -> {eval(P_LIST[i])}\n")
        f.close()


def gen_junit_tests():
    f=open("benchmark/testcode.txt", "w")
    for i in range(len(P_LIST)):
        test = f'''    @Test
    public void benchmarkTest{i+1}() {{
        final String path = "benchmark/benchmark{i + 1}.txt";
        runBenchmark(path);
    }}
'''
        f.write(test)

    f.close()

if __name__ == "__main__":
    gen_benchmarks()
    gen_junit_tests()
