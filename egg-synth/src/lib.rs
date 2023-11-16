use egg::*;
use libc::c_char;
use std::ffi::{CStr, CString};
use std::mem::{ManuallyDrop};

#[repr(C)]
pub struct FFIRule {
    name: *const c_char,
    left: *const c_char,
    right: *const c_char,
}

unsafe fn ptr_to_string(ptr: *const c_char) -> String {
    let bytes = CStr::from_ptr(ptr).to_bytes();
    String::from_utf8(bytes.to_vec()).unwrap()
}

define_language! {
    enum SimpleLanguage {
        Num(i32),
        "+" = Add([Id; 2]),
        "*" = Mul([Id; 2]),
        Symbol(Symbol),
    }
}

fn make_rules() -> Vec<Rewrite<SimpleLanguage, ()>> {
    vec![
        rewrite!("commute-add"; "(+ ?a ?b)" => "(+ ?b ?a)"),
        rewrite!("commute-mul"; "(* ?a ?b)" => "(* ?b ?a)"),
        rewrite!("add-0"; "(+ ?a 0)" => "?a"),
        rewrite!("mul-0"; "(* ?a 0)" => "0"),
        rewrite!("mul-1"; "(* ?a 1)" => "?a"),
    ]
}

#[no_mangle]
pub unsafe extern "C" fn simplify(s: *const c_char) -> *const c_char {
    // parse the expression, the type annotation tells it which Language to use
    let expr: RecExpr<SimpleLanguage> = CStr::from_ptr(s).to_str().unwrap().parse().unwrap();

    // simplify the expression using a Runner, which creates an e-graph with
    // the given expression and runs the given rules over it
    let runner = Runner::default().with_expr(&expr).run(&make_rules());

    // the Runner knows which e-class the expression given with `with_expr` is in
    let root = runner.roots[0];

    // use an Extractor to pick the best element of the root eclass
    let extractor = Extractor::new(&runner.egraph, AstSize);
    let (_, best) = extractor.find_best(root);
    // println!("Simplified {} to {} with cost {}", expr, best, best_cost);
    
    ManuallyDrop::new(CString::new(best.to_string()).unwrap()).as_ptr()
}

#[no_mangle]
pub unsafe extern "C" fn equal(l: *const c_char, r: *const c_char) -> bool {
    
    let l_expr: RecExpr<SimpleLanguage> = CStr::from_ptr(l).to_str().unwrap().parse().unwrap();
    let r_expr: RecExpr<SimpleLanguage> = CStr::from_ptr(r).to_str().unwrap().parse().unwrap();

    let mut runner = Runner::default().with_expr(&l_expr).with_expr(&r_expr).run(&make_rules());

    let l_id = runner.egraph.add_expr(&l_expr);

    let r_id = runner.egraph.add_expr(&r_expr);

    l_id == r_id

}

