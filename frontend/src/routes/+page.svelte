<script>
	import { isAuthenticated, currentUser, getRoleDashboard } from '$lib/stores/auth.js';

	const loggedIn = $derived($isAuthenticated);
	const user = $derived($currentUser);
	const dashboardUrl = $derived(user?.role ? getRoleDashboard(user.role) : '/dashboard/youth');

	const steps = [
		{
			step: '01',
			title: 'Corporate Funds Cohort',
			description: 'Corporates allocate enterprise development & B-BBEE funds directly into youth wage reserves.',
			tag: 'Corporate'
		},
		{
			step: '02',
			title: 'Youth Enters Hub',
			description: 'Trained youth program graduates enter SSH ready for real-world SME tasks.',
			tag: 'Youth'
		},
		{
			step: '03',
			title: 'SME Posts Free Task',
			description: 'Vetted small businesses post high-priority tasks with zero commission or fees.',
			tag: 'SME'
		},
		{
			step: '04',
			title: 'Youth Delivers Work',
			description: 'Matched candidates execute the task with guided milestones and quality standards.',
			tag: 'Youth'
		},
		{
			step: '05',
			title: 'SME Verifies Quality',
			description: 'The business rates the work and provides honest feedback on task completion.',
			tag: 'SME'
		},
		{
			step: '06',
			title: 'SSH Pays Directly',
			description: 'Wages disburse straight to the youth from the funded reserve. SMEs never handle cash.',
			tag: 'Fintech'
		},
		{
			step: '07',
			title: 'Ledger Records Proof',
			description: 'Verified work enters the Experience Ledger — portable, trusted proof of capability.',
			tag: 'Ledger'
		},
		{
			step: '08',
			title: 'Permanent Placement',
			description: 'Youth leverage their verified track record to transition into full-time employment.',
			tag: 'Alumni'
		}
	];
</script>

<svelte:head>
	<title>Shared Services Hub (SSH) | Work. Income. Experience. Guaranteed.</title>
</svelte:head>

<!-- Hero Section -->
<section class="relative overflow-hidden bg-gradient-to-b from-indigo-50/70 via-white to-slate-50 pt-16 pb-24 lg:pt-24 lg:pb-32">
	<!-- Decorative Background Gradients -->
	<div class="absolute inset-x-0 -top-40 -z-10 transform-gpu overflow-hidden blur-3xl sm:-top-80">
		<div
			class="relative left-[calc(50%-11rem)] aspect-[1155/678] w-[36.125rem] -translate-x-1/2 rotate-[30deg] bg-gradient-to-tr from-indigo-500 to-purple-400 opacity-25 sm:left-[calc(50%-30rem)] sm:w-[72.1875rem]"
		></div>
	</div>

	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 text-center">
		<!-- South African Context Pill -->
		<div class="inline-flex items-center gap-2 rounded-full border border-indigo-200/80 bg-white/80 px-4 py-1.5 text-xs font-semibold text-indigo-800 shadow-xs backdrop-blur-sm mb-8 animate-in fade-in slide-in-from-top-3">
			<span class="flex h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span>
			<span>Addressing South Africa's 4.7M Unemployed Youth</span>
			<span class="text-indigo-300">|</span>
			<span class="text-indigo-600 font-bold">The Three-Party Model</span>
		</div>

		<!-- Main Catchphrase Header -->
		<h1 class="text-4xl font-extrabold tracking-tight text-slate-900 sm:text-6xl lg:text-7xl max-w-5xl mx-auto leading-[1.12]">
			Work. Income. Experience. <br class="hidden sm:inline" />
			<span class="bg-gradient-to-r from-indigo-600 via-indigo-700 to-purple-600 bg-clip-text text-transparent">
				Guaranteed.
			</span>
		</h1>

		<p class="mx-auto mt-6 max-w-2xl text-lg sm:text-xl text-slate-600 leading-relaxed">
			Connecting youth program alumni to SMEs for paid tasks. Corporates fund the wages, SMEs get free talent, and the
			<span class="font-semibold text-indigo-700">Experience Ledger</span> builds verified proof of capability.
		</p>

		<!-- Call to Action Buttons -->
		<div class="mt-10 flex flex-wrap items-center justify-center gap-4">
			{#if loggedIn}
				<a
					href={dashboardUrl}
					class="inline-flex items-center justify-center rounded-xl bg-indigo-600 px-8 py-3.5 text-base font-bold text-white shadow-lg shadow-indigo-500/25 hover:bg-indigo-700 hover:shadow-indigo-500/35 transition-all active:scale-95"
				>
					<span>Go to My Dashboard</span>
					<svg class="ml-2 h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
					</svg>
				</a>
			{:else}
				<a
					href="/register"
					class="inline-flex items-center justify-center rounded-xl bg-indigo-600 px-8 py-3.5 text-base font-bold text-white shadow-lg shadow-indigo-500/25 hover:bg-indigo-700 hover:shadow-indigo-500/35 transition-all active:scale-95"
				>
					<span>Register as Youth or SME</span>
					<svg class="ml-2 h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
					</svg>
				</a>
				<a
					href="/login"
					class="inline-flex items-center justify-center rounded-xl border border-slate-300 bg-white px-8 py-3.5 text-base font-bold text-slate-700 shadow-xs hover:bg-slate-50 hover:text-indigo-600 transition-all active:scale-95"
				>
					Sign In
				</a>
			{/if}

			<a
				href="/tasks"
				class="inline-flex items-center justify-center rounded-xl border border-indigo-200 bg-indigo-50/50 px-6 py-3.5 text-base font-bold text-indigo-700 hover:bg-indigo-100/60 transition-all"
			>
				Browse Open Tasks
			</a>
		</div>

		<!-- The Triple Guarantee Badges -->
		<div class="mt-16 grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl mx-auto text-left">
			<div class="rounded-2xl border border-slate-200 bg-white/90 p-6 shadow-xs backdrop-blur-xs">
				<div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-100 text-blue-700 font-bold mb-3">
					01
				</div>
				<h2 class="text-base font-bold text-slate-900">Guaranteed Work</h2>
				<p class="mt-1 text-xs text-slate-600 leading-relaxed">
					SSH actively matches youth to tasks. If no SME tasks exist, SSH creates internal tasks to guarantee activity.
				</p>
			</div>

			<div class="rounded-2xl border border-slate-200 bg-white/90 p-6 shadow-xs backdrop-blur-xs">
				<div class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-100 text-emerald-700 font-bold mb-3">
					02
				</div>
				<h2 class="text-base font-bold text-slate-900">Guaranteed Income</h2>
				<p class="mt-1 text-xs text-slate-600 leading-relaxed">
					Corporates fund youth wages. SSH pays youth directly. SMEs never handle cash, eliminating wage disputes.
				</p>
			</div>

			<div class="rounded-2xl border border-slate-200 bg-white/90 p-6 shadow-xs backdrop-blur-xs">
				<div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-100 text-purple-700 font-bold mb-3">
					03
				</div>
				<h2 class="text-base font-bold text-slate-900">Verified Experience</h2>
				<p class="mt-1 text-xs text-slate-600 leading-relaxed">
					The Experience Ledger records every SME verification. Youth graduate into employment with portable proof.
				</p>
			</div>
		</div>
	</div>
</section>

<!-- The Three-Party Model Section -->
<section class="py-20 bg-white border-y border-slate-200/80">
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
		<div class="text-center max-w-3xl mx-auto mb-16">
			<h2 class="text-xs font-bold uppercase tracking-widest text-indigo-600 mb-2">Sustainable Ecosystem</h2>
			<h3 class="text-3xl font-extrabold text-slate-900 sm:text-4xl">
				The Three-Party Model
			</h3>
			<p class="mt-3 text-slate-600 text-base leading-relaxed">
				How SSH aligns corporates, SMEs, and young South Africans without exploitation or friction.
			</p>
		</div>

		<div class="grid grid-cols-1 md:grid-cols-3 gap-8">
			<!-- 1. Youth Alumni -->
			<div class="relative rounded-3xl border border-slate-200 bg-gradient-to-b from-slate-50 to-white p-8 shadow-xs hover:border-indigo-300 hover:shadow-lg transition-all">
				<div class="inline-flex rounded-2xl bg-indigo-100 p-3 text-indigo-600 mb-6">
					<svg class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
					</svg>
				</div>
				<div class="text-xs font-bold uppercase tracking-wider text-indigo-600 mb-1">Beneficiary & Talent</div>
				<h4 class="text-2xl font-bold text-slate-900 mb-3">Youth Program Alumni</h4>
				<ul class="space-y-3 text-sm text-slate-600">
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Access paid micro-tasks suited to trained skills</span>
					</li>
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Earn guaranteed wages paid directly from reserve funds</span>
					</li>
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Build a verifiable, employer-trusted Experience Ledger</span>
					</li>
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Stay active until securing permanent placement</span>
					</li>
				</ul>
				<div class="mt-8 pt-6 border-t border-slate-100">
					<a href="/register" class="text-sm font-bold text-indigo-600 hover:text-indigo-800 flex items-center gap-1.5">
						<span>Apply as Youth Candidate</span>
						<span>→</span>
					</a>
				</div>
			</div>

			<!-- 2. SMEs -->
			<div class="relative rounded-3xl border-2 border-indigo-600 bg-white p-8 shadow-xl shadow-indigo-500/5 hover:border-indigo-700 transition-all">
				<div class="absolute -top-3 right-6 rounded-full bg-indigo-600 px-3 py-0.5 text-xs font-bold text-white uppercase tracking-wider shadow-xs">
					Zero Cost
				</div>
				<div class="inline-flex rounded-2xl bg-amber-100 p-3 text-amber-700 mb-6">
					<svg class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
					</svg>
				</div>
				<div class="text-xs font-bold uppercase tracking-wider text-amber-700 mb-1">Capacity & Growth</div>
				<h4 class="text-2xl font-bold text-slate-900 mb-3">South African SMEs</h4>
				<ul class="space-y-3 text-sm text-slate-600">
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Post operational tasks with zero hiring costs or fees</span>
					</li>
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Get immediate support in tech, marketing, and finance</span>
					</li>
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Never worry about payroll administration or disputes</span>
					</li>
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Review and verify completed work to unlock payouts</span>
					</li>
				</ul>
				<div class="mt-8 pt-6 border-t border-slate-100">
					<a href="/register" class="text-sm font-bold text-indigo-600 hover:text-indigo-800 flex items-center gap-1.5">
						<span>Register SME for Free</span>
						<span>→</span>
					</a>
				</div>
			</div>

			<!-- 3. Corporates -->
			<div class="relative rounded-3xl border border-slate-200 bg-gradient-to-b from-slate-50 to-white p-8 shadow-xs hover:border-indigo-300 hover:shadow-lg transition-all">
				<div class="inline-flex rounded-2xl bg-emerald-100 p-3 text-emerald-700 mb-6">
					<svg class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
					</svg>
				</div>
				<div class="text-xs font-bold uppercase tracking-wider text-emerald-700 mb-1">Funding & Compliance</div>
				<h4 class="text-2xl font-bold text-slate-900 mb-3">Corporate Sponsors</h4>
				<ul class="space-y-3 text-sm text-slate-600">
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Fund cohorts of youth directly via ESD / CSI budgets</span>
					</li>
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Earn verifiable B-BBEE scorecard points and proof</span>
					</li>
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Real-time impact dashboards showing every Rand spent</span>
					</li>
					<li class="flex items-start gap-2.5">
						<span class="text-emerald-600 font-bold">✓</span>
						<span>Audit-ready reporting with immutable verified entries</span>
					</li>
				</ul>
				<div class="mt-8 pt-6 border-t border-slate-100">
					<a href="/register" class="text-sm font-bold text-indigo-600 hover:text-indigo-800 flex items-center gap-1.5">
						<span>Partner as Corporate</span>
						<span>→</span>
					</a>
				</div>
			</div>
		</div>
	</div>
</section>

<!-- How It Works: 8-Step Flow -->
<section class="py-20 bg-slate-50">
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
		<div class="text-center max-w-3xl mx-auto mb-16">
			<h2 class="text-xs font-bold uppercase tracking-widest text-indigo-600 mb-2">End-to-End Execution</h2>
			<h3 class="text-3xl font-extrabold text-slate-900 sm:text-4xl">
				How SSH Operates: The 8-Step Flow
			</h3>
			<p class="mt-3 text-slate-600 text-base">
				From corporate reserve funding all the way to permanent placement in the mainstream economy.
			</p>
		</div>

		<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
			{#each steps as s}
				<div class="rounded-2xl border border-slate-200/80 bg-white p-6 shadow-xs hover:border-indigo-300 hover:shadow-md transition-all">
					<div class="flex items-center justify-between mb-4">
						<span class="text-2xl font-black text-indigo-600/40">{s.step}</span>
						<span class="rounded-md bg-slate-100 px-2 py-0.5 text-[10px] font-bold text-slate-600 uppercase">
							{s.tag}
						</span>
					</div>
					<h4 class="text-base font-bold text-slate-900 mb-2">{s.title}</h4>
					<p class="text-xs text-slate-600 leading-relaxed">{s.description}</p>
				</div>
			{/each}
		</div>
	</div>
</section>

<!-- Call to Action Banner -->
<section class="relative overflow-hidden bg-indigo-900 py-16 sm:py-20">
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 text-center relative z-10">
		<h3 class="text-3xl font-extrabold text-white sm:text-4xl">
			Ready to unlock guaranteed work and verified capability?
		</h3>
		<p class="mx-auto mt-4 max-w-2xl text-base text-indigo-200 leading-relaxed">
			Join hundreds of youth program alumni and growing South African SMEs on the Shared Services Hub today.
		</p>
		<div class="mt-8 flex flex-wrap items-center justify-center gap-4">
			<a
				href="/register"
				class="rounded-xl bg-white px-8 py-3.5 text-base font-bold text-indigo-900 shadow-md hover:bg-indigo-50 transition-all active:scale-95"
			>
				Create Your Account
			</a>
			<a
				href="/login"
				class="rounded-xl border border-indigo-400/50 bg-indigo-800/60 px-8 py-3.5 text-base font-bold text-white hover:bg-indigo-800 transition-all"
			>
				Sign In
			</a>
		</div>
	</div>
</section>
